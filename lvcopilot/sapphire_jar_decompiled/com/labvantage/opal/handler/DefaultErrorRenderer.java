/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 *  org.jsoup.Jsoup
 *  org.jsoup.nodes.Document
 *  org.jsoup.nodes.Document$OutputSettings
 *  org.jsoup.safety.Safelist
 */
package com.labvantage.opal.handler;

import com.labvantage.opal.handler.ErrorRenderer;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.pageelements.controls.Button;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.servlet.jsp.PageContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import sapphire.accessor.TranslationProcessor;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.JstlUtil;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DefaultErrorRenderer
implements ErrorRenderer {
    public String LABVANTAGE_CVS_ID = "$Revision: 91476 $";
    private ErrorHandler errorHandler = null;
    private String __SDIFormName;
    private List __ValidateErrors = new ArrayList();
    private List __ConfirmErrors = new ArrayList();
    private List __InfoErrors = new ArrayList();
    private TranslationProcessor __Tp;
    private Browser browser;
    private static Safelist whitelist;

    @Override
    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public String getHTML(PageContext pageContext) {
        StringBuffer html = new StringBuffer();
        this.__Tp = new TranslationProcessor(pageContext);
        if (pageContext != null) {
            this.browser = new Browser(pageContext);
        }
        if (this.errorHandler != null && this.errorHandler.size() > 0) {
            this.parseErrors();
            html.append("<table border=0 cellpadding=5 cellspacing=0><tr><td>");
            if (this.hasOnlyInfoMsg()) {
                html.append(this.getInfoOnlyTableTag());
            } else {
                html.append(this.getTableTag());
            }
            html.append(this.getTitleRow(pageContext));
            html.append("<tr><td>");
            html.append(this.getErrorTable());
            html.append("</td></tr>");
            if (this.showButtons()) {
                html.append(this.getButtonsRow(pageContext));
            }
            html.append("</table>");
            html.append("</table>");
            html.append(this.getJavaScript(pageContext));
        }
        return html.toString();
    }

    private String getInfoOnlyTableTag() {
        return "<table width=\"" + (this.browser != null && this.browser.isPhone() ? "350" : "600") + "px\" class=\"maintform_table\">";
    }

    private String getTableTag() {
        return "<table width=\"" + (this.browser != null && this.browser.isPhone() ? "350" : "600") + "px\" style=\"border:1px solid black\">";
    }

    @Override
    public String getPageHTML(PageContext pageContext) {
        StringBuffer html = new StringBuffer();
        if (pageContext != null) {
            this.browser = new Browser(pageContext);
        }
        if (this.errorHandler != null && this.errorHandler.size() > 0) {
            this.parseErrors();
            html.append("<table border=0 cellpadding=5 cellspacing=0><tr><td>");
            if (this.hasOnlyInfoMsg()) {
                html.append(this.getInfoOnlyTableTag());
            } else {
                html.append(this.getTableTag());
            }
            html.append(this.getTitleRow(pageContext));
            html.append("<tr><td>");
            html.append(this.getErrorTable());
            html.append("</td></tr>");
            if (this.showButtons()) {
                html.append(this.getButtonsRow(pageContext));
            }
            html.append("</table>");
            html.append("</table>");
        }
        return html.toString();
    }

    @Override
    public String getFormHTML(PageContext pageContext) {
        StringBuffer buffer = new StringBuffer();
        if (pageContext != null) {
            this.browser = new Browser(pageContext);
        }
        if (this.showButtons() && this.__SDIFormName == null) {
            PropertyList pagedata = (PropertyList)JstlUtil.evaluateExpression("${pagedata}", pageContext);
            Set keySet = pagedata.keySet();
            buffer.append("\n<form id='").append("ruleHandlerForm").append("' name='").append("ruleHandlerForm").append("' method='post'>");
            for (String key : keySet) {
                if (pagedata.get(key) instanceof PropertyListCollection) continue;
                String value = pagedata.getProperty(key);
                if (value.length() > 0 && ("querywhere".equals(key) || "restrictivewhere".equals(key))) {
                    value = EncryptDecrypt.obfsql(value);
                }
                if (value == null || value.length() <= 0) continue;
                buffer.append("\n<input type=\"hidden\" name=\"").append(key).append("\" value=\"");
                buffer.append(value);
                buffer.append("\" id=\"").append(key).append("\">");
            }
            buffer.append("\n</form>");
        }
        return buffer.toString();
    }

    @Override
    public void setSDIFormName(String sdiFormName) {
        this.__SDIFormName = sdiFormName;
    }

    @Override
    public String getSDIFormName() {
        return this.__SDIFormName;
    }

    private String getTitleRow(PageContext pageContext) {
        StringBuffer titleRow = new StringBuffer();
        titleRow.append(this.getTitleRow());
        if (!this.hasOnlyInfoMsg() && this.showButtons()) {
            pageContext.setAttribute("__ruleViolationFlag", (Object)"Y");
        }
        return titleRow.toString();
    }

    public boolean hasOnlyInfoMsg() {
        return this.getCountConfirmMsg() == 0 && this.getCountValidationMsg() == 0 && this.getCountInfoMsg() > 0;
    }

    private void parseErrors() {
        ErrorDetail error = null;
        for (int i = 0; i < this.errorHandler.size(); ++i) {
            error = (ErrorDetail)this.errorHandler.get(i);
            if (error == null) continue;
            String errorType = error.getErrorType();
            if (errorType.equals("CONFIRM")) {
                this.__ConfirmErrors.add(error);
                continue;
            }
            if (errorType.equals("VALIDATION")) {
                this.__ValidateErrors.add(error);
                continue;
            }
            if (errorType.equals("INFORMATION")) {
                this.__InfoErrors.add(error);
                continue;
            }
            if (!errorType.equals("FAILURE")) continue;
            this.__ValidateErrors.add(error);
        }
    }

    private String getErrorTable() {
        StringBuffer sb = new StringBuffer();
        String style = "";
        if (this.browser != null && this.browser.isFireFox()) {
            style = "width:1000px;";
        }
        sb.append("<table width='100%' border=0 cellpadding=2 cellspacing=0 id='__ruleErrorTable'>");
        if (this.__ValidateErrors.size() > 0) {
            sb.append("<tr><td style='").append(style).append("border:1px solid red;background:#FAEBD7' valign='top'>");
            String title = this.__ValidateErrors.size() > 1 ? this.__Tp.translate("Errors") : this.__Tp.translate("Error");
            sb.append("<b><font color='red'>").append(title);
            sb.append("</font></b></td></tr>");
            sb.append("<tr><td style='border:1px solid red' valign='top'>");
            sb.append(this.getValidationErrorsTable());
            sb.append("</td></tr>");
            sb.append("<tr><td height='5'></td></tr>");
        }
        if (this.__ConfirmErrors.size() > 0) {
            sb.append("<tr><td style='").append(style).append("border:1px solid blue;background:#B0C4DE' valign='top'>");
            sb.append("<b><font color='blue'> ").append(this.__Tp.translate("Confirmation Required") + " ");
            sb.append("</font></b></td></tr>");
            sb.append("<tr><td style='border:1px solid blue' valign='top'>");
            sb.append(this.getConfirmationErrorsTable());
            sb.append("</td></tr>");
            sb.append("<tr><td height='5'></td></tr>");
        }
        if (this.__InfoErrors.size() > 0) {
            sb.append("<tr><td style='").append(style).append("border:1px solid green;background:#c8ffc8' valign='top'>");
            sb.append("<b><font color='green'> ").append(this.__Tp.translate("Information") + " ");
            sb.append("</font></b></td></tr>");
            sb.append("<tr><td style='border:1px solid green' valign='top'>");
            sb.append(this.getInformationErrorsTable());
            sb.append("</td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private boolean showButtons() {
        if (this.getCountValidationMsg() > 0) {
            return false;
        }
        return this.getCountConfirmMsg() > 0;
    }

    private String getValidationErrorsTable() {
        StringBuffer sb = new StringBuffer();
        sb.append("<table cellpadding=2 cellspacing=0 border=0 width='100%'>");
        for (int i = 0; i < this.__ValidateErrors.size(); ++i) {
            ErrorDetail error = (ErrorDetail)this.__ValidateErrors.get(i);
            sb.append("<tr><td valign='top' NOWRAP><b><font color='red'>");
            sb.append(i + 1).append(") ");
            if (this.__Tp != null) {
                sb.append(this.__Tp.translate(error.getErrorid()));
            } else {
                sb.append(error.getErrorid());
            }
            sb.append("</font></b></td><td valign='top'>");
            if (error.getMessage().indexOf("LV_MessageLogList") > 0) {
                sb.append(DefaultErrorRenderer.format(error.getMessage()));
            } else if (this.__Tp != null) {
                sb.append(DefaultErrorRenderer.sanitizeErrorMessage(this.__Tp.translate(DefaultErrorRenderer.format(error.getMessage()))));
            } else {
                sb.append(DefaultErrorRenderer.sanitizeErrorMessage(DefaultErrorRenderer.format(error.getMessage())));
            }
            sb.append("</td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private String getConfirmationErrorsTable() {
        StringBuffer sb = new StringBuffer();
        sb.append("<table cellpadding=2 cellspacing=0 border=0 width='100%'>");
        for (int i = 0; i < this.__ConfirmErrors.size(); ++i) {
            ErrorDetail error = (ErrorDetail)this.__ConfirmErrors.get(i);
            sb.append("<tr><td valign='top' NOWRAP><b><font color='blue'>");
            sb.append(i + 1).append(") ");
            if (this.__Tp != null) {
                sb.append(this.__Tp.translate(error.getErrorid()));
            } else {
                sb.append(error.getErrorid());
            }
            sb.append("</font></b></td><td valign='top'>");
            sb.append(DefaultErrorRenderer.sanitizeErrorMessage(DefaultErrorRenderer.format(error.getMessage())));
            sb.append("</td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private String getInformationErrorsTable() {
        StringBuffer sb = new StringBuffer();
        sb.append("<table cellpadding=2 cellspacing=0 border=0 width='100%'>");
        for (int i = 0; i < this.__InfoErrors.size(); ++i) {
            ErrorDetail error = (ErrorDetail)this.__InfoErrors.get(i);
            sb.append("<tr><td valign='top' NOWRAP><b><font color='green'>");
            sb.append(i + 1).append(") ");
            if (this.__Tp != null) {
                sb.append(this.__Tp.translate(error.getErrorid()));
            } else {
                sb.append(error.getErrorid());
            }
            sb.append("</font></b></td><td valign='top'>");
            sb.append(DefaultErrorRenderer.format(error.getMessage()));
            sb.append("</td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private String getButtonsRow(PageContext pageContext) {
        StringBuffer buttonsRow = new StringBuffer();
        buttonsRow.append("<tr>");
        buttonsRow.append("<td align=right>");
        buttonsRow.append("<table border=0><tr><td>");
        buttonsRow.append(this.getConfirmButtonCell(pageContext));
        buttonsRow.append("</td><td>");
        buttonsRow.append(this.getCancelButtonCell(pageContext));
        buttonsRow.append("</td></tr></table>");
        buttonsRow.append("</td>");
        buttonsRow.append("</tr>");
        return buttonsRow.toString();
    }

    private String getConfirmButtonCell(PageContext pageContext) {
        Button button = new Button(pageContext);
        button.setText(this.__Tp.translate("Confirm"));
        button.setImg("WEB-OPAL/images/yes.gif");
        button.setWidth("60");
        button.setTip(this.__Tp.translate("Confirm"));
        button.setAction("ruleViolationConfirmHandler()");
        return button.getHtml();
    }

    private String getCancelButtonCell(PageContext pageContext) {
        Button button = new Button(pageContext);
        button.setText(this.__Tp.translate("Cancel"));
        button.setImg("WEB-CORE/images/gif/Delete.gif");
        button.setWidth("60");
        button.setTip(this.__Tp.translate("Cancel"));
        button.setAction("ruleViolationCancelHandler()");
        return button.getHtml();
    }

    @Override
    public String getPageScript(PageContext pageContext) {
        if (this.errorHandler != null && this.errorHandler.size() > 0) {
            return this.getJavaScript(pageContext);
        }
        return "";
    }

    private String getJavaScript(PageContext pageContext) {
        StringBuffer js = new StringBuffer();
        String formName = this.__SDIFormName != null ? this.__SDIFormName : "ruleHandlerForm";
        js.append("<script language='javascript'>");
        if (this.showButtons()) {
            js.append("\nfunction ").append("ruleViolationConfirmHandler").append("(){");
            PropertyList pagedata = (PropertyList)JstlUtil.evaluateExpression("${pagedata}", pageContext);
            pagedata.setProperty("__sdcruleconfirm", "Y");
            js.append("\n  var ruleHandlerForm = document.getElementById( '").append(formName).append("' );");
            js.append("\n  if ( typeof( ruleHandlerForm.__pr_extraprops ) == 'undefined' ) {");
            js.append("\n      var input = document.createElement('INPUT');");
            js.append("\n      input.type = 'hidden';");
            js.append("\n      input.name = '__pr_extraprops';");
            js.append("\n      input.id = '__pr_extraprops';");
            js.append("\n      ruleHandlerForm.appendChild(input);");
            js.append("\n  }");
            js.append("\n  if ( ruleHandlerForm.__pr_extraprops.value == '' ) {");
            js.append("\n      ruleHandlerForm.__pr_extraprops.value = '__sdcruleconfirm=Y';");
            js.append("\n  } else {");
            js.append("\n      ruleHandlerForm.__pr_extraprops.value = ruleHandlerForm.__pr_extraprops.value + ';__sdcruleconfirm=Y';");
            js.append("\n  }");
            js.append("\n      try {");
            js.append("\n          if( document.getElementById( '").append(formName).append("' ) != null ) {");
            js.append("\n              document.getElementById( 'sdidiv' ).disabled = false;");
            if (this.__SDIFormName != null) {
                js.append("\n            save();");
            } else {
                js.append("\n            var actiontransactionid = document.getElementById( 'actiontransactionid' );");
                js.append("\n            if ( actiontransactionid != undefined ) {");
                js.append("\n                actiontransactionid.value = '';");
                js.append("\n            }");
                js.append("\n            ruleHandlerForm.submit();");
            }
            js.append("\n          }");
            js.append("\n      }");
            js.append("\n      catch( err ) {");
            js.append("\n          top.showMessage( err.message );");
            js.append("\n      }");
            js.append("\n      return;");
            js.append("\n  }");
            js.append("\nfunction ").append("ruleViolationCancelHandler").append("(){");
            js.append("\n    var sdiDiv = document.getElementById( 'sdidiv' );");
            js.append("\n    var errorDiv = document.getElementById( 'errordiv' );");
            js.append("\n    if( errorDiv != null ) {");
            js.append("\n        errorDiv.style.display = 'none';");
            js.append("\n    }");
            js.append("\n    if( sdiDiv != null ) {");
            js.append("\n        sdiDiv.disabled = false;");
            js.append("\n        sdiDiv.style.display = 'block'");
            js.append("\n    }");
            js.append("\n    return;");
            js.append("\n}");
        } else {
            js.append(this.getToggleErrorTableScript());
            RequestContext rc = null;
            if (pageContext != null) {
                rc = RequestContext.getRequestContext(pageContext);
            }
            js.append("document.getElementById( '__ruleErrorTable' ).style.display = sapphire.page.html5?'table':'block';");
        }
        js.append("</script>");
        return js.toString();
    }

    public int getCountValidationMsg() {
        return this.__ValidateErrors.size();
    }

    public int getCountConfirmMsg() {
        return this.__ConfirmErrors.size();
    }

    public int getCountInfoMsg() {
        return this.__InfoErrors.size();
    }

    @Override
    public String getValidationHTML(TranslationProcessor tp) {
        return this.getValidationHTML(tp, true);
    }

    @Override
    public String getValidationHTML(TranslationProcessor tp, boolean showButtons) {
        StringBuffer html = new StringBuffer();
        this.__Tp = tp;
        if (this.errorHandler != null && this.errorHandler.size() > 0) {
            this.parseErrors();
            html.append("<table border=0 cellpadding=5 cellspacing=0><tr><td>");
            if (this.hasOnlyInfoMsg()) {
                html.append(this.getInfoOnlyTableTag());
            } else {
                html.append(this.getTableTag());
            }
            html.append(this.getTitleRow());
            html.append("<tr><td>");
            html.append(this.getErrorTable());
            html.append("</td></tr>");
            html.append(showButtons ? this.getValidationButtonsRow() : "");
            html.append(this.getValidationJavaScript());
            html.append("</table>");
            html.append("</table>");
        }
        return html.toString();
    }

    private String getTitleRow() {
        StringBuffer titleRow = new StringBuffer();
        if (this.hasOnlyInfoMsg()) {
            titleRow.append("<tr><td class='maintform_fieldtitle_blue'>");
            titleRow.append("<img src='WEB-OPAL/images/minus.gif' onClick='toggleErrorTable( this );' style='cursor: pointer'>&nbsp;");
            String title = this.errorHandler.size() > 1 ? this.__Tp.translate("Messages") : this.__Tp.translate("Message");
            titleRow.append("<b><strong>").append(title).append("</strong></b>");
            titleRow.append("</td></tr>");
        } else {
            titleRow.append("<tr><td class='field_error'>");
            if (!this.showButtons()) {
                titleRow.append("<img src='WEB-OPAL/images/minus.gif' onClick='toggleErrorTable( this );' style='cursor: pointer'>&nbsp;");
            }
            titleRow.append("<b><font color=red><strong>").append(this.__Tp.translate("Violations: Operation Unsuccessful")).append("</strong></b>");
            titleRow.append("</font>");
            titleRow.append("</td></tr>");
        }
        return titleRow.toString();
    }

    private String getValidationButtonsRow() {
        StringBuffer buttonsRow = new StringBuffer();
        buttonsRow.append("<tr>");
        buttonsRow.append("<td align=right>");
        buttonsRow.append("<table border=0><tr>");
        if (this.getCountValidationMsg() > 0) {
            buttonsRow.append("<td>").append(this.getButtonHTML("Close", "cancelValidation()")).append("</td>");
        } else if (this.getCountConfirmMsg() > 0) {
            buttonsRow.append("<td>").append(this.getButtonHTML("OK", "continueValidation()")).append("</td>").append("<td>").append(this.getButtonHTML("Cancel", "cancelValidation()")).append("</td>");
        } else if (this.getCountInfoMsg() > 0) {
            buttonsRow.append("<td>").append(this.getButtonHTML("OK", "continueValidation()")).append("</td>");
        }
        buttonsRow.append("</tr>");
        buttonsRow.append("</td>");
        buttonsRow.append("</tr>");
        return buttonsRow.toString();
    }

    private String getButtonHTML(String buttonText, String buttonAction) {
        StringBuffer button = new StringBuffer();
        button.append("<input type=button value=\"").append(SafeHTML.encodeForHTMLAttribute(this.__Tp.translate(buttonText))).append("\" ").append("onclick = \"").append(SafeHTML.encodeForHTMLAttribute(buttonAction)).append("\" /> ");
        return button.toString();
    }

    private String getValidationJavaScript() {
        StringBuffer validationJS = new StringBuffer();
        validationJS.append("<script language=\"JavaScript\" > ").append(this.getToggleErrorTableScript()).append("</script>");
        return validationJS.toString();
    }

    private String getToggleErrorTableScript() {
        StringBuffer js = new StringBuffer();
        js.append("function toggleErrorTable( e ) {");
        js.append("    var errorTable = document.getElementById( '__ruleErrorTable' );");
        js.append("    if ( errorTable.style.display == 'none' ) {");
        js.append("        errorTable.style.display = sapphire.page.html5?'table':'block';");
        js.append("        e.src = 'WEB-OPAL/images/minus.gif';");
        js.append("    }");
        js.append("    else {");
        js.append("        errorTable.style.display = 'none';");
        js.append("        e.src = 'WEB-OPAL/images/plus.gif';");
        js.append("    }");
        js.append("\tif( typeof( initScrollGrid ) != 'undefined' ) { ");
        js.append("\t\tinitScrollGrid();");
        js.append("\t}");
        js.append("}");
        return js.toString();
    }

    private static String format(String str) {
        str = str.trim();
        while (str.endsWith("|")) {
            str = str.substring(0, str.length() - 1);
        }
        if (str.indexOf("<") < 0 && str.indexOf(">") < 0) {
            str = StringUtil.replaceAll(str, "\n", "<br>");
        }
        return str.trim();
    }

    public static String sanitizeErrorMessage(String errorMsg) {
        Document doc = Jsoup.parse((String)errorMsg);
        doc.outputSettings().prettyPrint(false);
        if (whitelist == null) {
            DefaultErrorRenderer.setUpWhitelist();
        }
        if (!Jsoup.isValid((String)errorMsg, (Safelist)whitelist)) {
            return Jsoup.clean((String)errorMsg, (String)"", (Safelist)whitelist, (Document.OutputSettings)doc.outputSettings());
        }
        return errorMsg;
    }

    public static void setUpWhitelist() {
        whitelist = Safelist.none();
        DefaultErrorRenderer.copySafeHtmlTagsToWhitelist();
        whitelist.addTags(new String[]{"font", "strong", "u", "i", "div", "span", "p", "table", "td", "tr"});
        whitelist.addAttributes("font", new String[]{"size", "color", "face"});
        whitelist.addAttributes("table", new String[]{"border", "class", "cellspacing"});
        whitelist.addAttributes("td", new String[]{"class"});
    }

    public static void copySafeHtmlTagsToWhitelist() {
        String[] allowedHtmlTags = SafeHTML.getAllowedHtmlTags();
        Arrays.stream(allowedHtmlTags).filter(tags -> !tags.contains("/")).map(tags -> tags.replace("<", "").replace(">", "")).forEach(tags -> whitelist.addTags(new String[]{tags}));
    }
}

