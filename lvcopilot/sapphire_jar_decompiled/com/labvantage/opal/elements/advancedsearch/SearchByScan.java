/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.elements.advancedsearch;

import com.labvantage.sapphire.pageelements.controls.Button;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.xml.PropertyList;

public class SearchByScan
extends BaseElement {
    private String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private String __SdcId = "";
    private String __MatchCol = "";
    private String __ShowAddButton = "";
    private String __ShowResetButton = "";
    private boolean __Debug = false;
    private String __Height = "";
    private String __Callback = "";
    private TranslationProcessor __Tp;

    public SearchByScan(PropertyList element) {
        this.element = element;
        this.setTranslationProcessor();
    }

    public SearchByScan(PageContext pageContext, String connectionid) {
        this.pageContext = pageContext;
        this.setConnectionId(connectionid);
        this.setTranslationProcessor();
    }

    private void setTranslationProcessor() {
        this.__Tp = new TranslationProcessor(this.pageContext);
    }

    @Override
    public String getHtml() {
        this.__SdcId = this.element.getProperty("sdcid");
        this.__MatchCol = this.element.getPropertyList("scansearch") != null ? this.element.getPropertyList("scansearch").getProperty("matchcolumn") : "";
        this.__ShowAddButton = this.element.getPropertyList("scansearch") != null ? this.element.getPropertyList("scansearch").getProperty("showaddbutton") : "";
        this.__ShowResetButton = this.element.getPropertyList("scansearch") != null ? this.element.getPropertyList("scansearch").getProperty("showresetbutton") : "";
        this.__Height = this.element.getPropertyList("scansearch") != null ? this.element.getPropertyList("scansearch").getProperty("height") : "";
        this.__Callback = this.element.getProperty("callback").equalsIgnoreCase("") ? "showResult" : this.element.getProperty("callback");
        return this.getScanHTML().toString() + this.getJavaScript().toString();
    }

    private StringBuffer getScanHTML() {
        StringBuffer sbHtml = new StringBuffer("");
        Button btnOk = new Button(this.pageContext);
        Button btnAdd = new Button(this.pageContext);
        Button btnClear = new Button(this.pageContext);
        btnOk.setAppearance("scansearch");
        btnOk.setText(this.__Tp.translate("OK"));
        btnOk.setAction("SearchByScanId()");
        btnOk.setMargin("none");
        btnOk.setStyle("width:25px; margin-left:3px; height:18px; ");
        if (this.__ShowAddButton.equalsIgnoreCase("") || this.__ShowAddButton.equalsIgnoreCase("Y")) {
            btnAdd.setAppearance("scansearch");
            btnAdd.setText(this.__Tp.translate("Add"));
            btnAdd.setAction("addManual()");
            btnAdd.setMargin("none");
            btnAdd.setStyle("width:35px; height:18px; ");
        }
        if (this.__ShowResetButton.equalsIgnoreCase("") || this.__ShowResetButton.equalsIgnoreCase("Y")) {
            btnClear.setAppearance("scansearch");
            btnClear.setText(this.__Tp.translate("Clear"));
            btnClear.setAction("removeall()");
            btnClear.setMargin("none");
            btnClear.setStyle("width:35px; height:18px; ");
        }
        sbHtml.append("\n<!-- START PAINTING THE SCAN SEARCH ELEMENT -->\n");
        sbHtml.append("\n<form name=\"frmScanSearch\" id=\"frmScanSearch\" style=\"padding:0px; margin:0px;\">\n");
        sbHtml.append("\t<input type=\"hidden\" name=\"keyid1\" value=\"\">\n");
        sbHtml.append("\t<input type=\"hidden\" name=\"forsqlwhere\" value=\"\">\n");
        sbHtml.append("\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");
        sbHtml.append("\t<tr>\n");
        sbHtml.append("     <td align=\"left\"> <input type=\"text\" class=input_scansearch name=\"manualkeyid\" value=\"\" onKeyDown=\"manualKeyPress()\"></td>\n");
        sbHtml.append("\t\t<td valign=\"middle\" align=\"left\">" + btnOk.getHtml() + "</td>\n");
        sbHtml.append("\t</tr>\n");
        sbHtml.append("\t<tr>\n");
        sbHtml.append("\t    <td colspan=2>\n");
        sbHtml.append("         <table cellspacing=0 cellpadding=0 style=\"padding-top:2px;padding-bottom:2px;padding-left:0px\"><tr>\n");
        if (this.__ShowAddButton.equalsIgnoreCase("") || this.__ShowAddButton.equalsIgnoreCase("Y")) {
            sbHtml.append("\t\t    <td valign=\"top\" align=\"left\">" + btnAdd.getHtml() + "</td><td>&nbsp;</td>\n");
        }
        if (this.__ShowResetButton.equalsIgnoreCase("") || this.__ShowResetButton.equalsIgnoreCase("Y")) {
            sbHtml.append("\t\t    <td valign=\"top\" align=\"left\">" + btnClear.getHtml() + "</td>\n");
        }
        sbHtml.append("\t        </tr></table>\n");
        sbHtml.append("     </td>\n");
        sbHtml.append("\t</tr>\n");
        sbHtml.append("\t<tr>\n");
        sbHtml.append("\t\t<td valign=\"top\" colspan=2 align=\"left\">\n");
        sbHtml.append("\t\t    <div id=\"manualkeyids\" style=\"display:block;margin:2px;overflow:auto;width:100%;height:" + this.__Height + "px\"></div>\n");
        sbHtml.append("</td></tr>\n");
        sbHtml.append("</table>\n");
        sbHtml.append("</form>\n");
        sbHtml.append("<!-- END PAINTING THE SCAN SEARCH ELEMENT -->\n\n");
        btnOk = null;
        btnAdd = null;
        btnClear = null;
        return sbHtml;
    }

    private StringBuffer getJavaScript() {
        StringBuffer sbJavaScript = new StringBuffer();
        SDCProcessor sdcProc = new SDCProcessor(this.getConnectionId());
        HashMap hmSdcProps = sdcProc.getSDCProperties(this.__SdcId);
        String col = "";
        col = this.__MatchCol != null && this.__MatchCol.length() > 0 ? this.__MatchCol : (String)hmSdcProps.get("keycolid1");
        sbJavaScript.append("<script language=\"JavaScript\">\n");
        sbJavaScript.append("var keyidlist = new Array();\n");
        sbJavaScript.append("var keyidlistcount = 0;\n");
        sbJavaScript.append("var sqlw = \"'\";\n");
        sbJavaScript.append("\n");
        sbJavaScript.append("function refreshManual() {\n");
        sbJavaScript.append("\tfrmScanSearch.keyid1.value = \"\";\n");
        sbJavaScript.append("\tfrmScanSearch.forsqlwhere.value = \"\";\n");
        sbJavaScript.append("\tvar\ttemp = \"<table border='0' width=100% style=\\\"font-size:7.5pt;\\\">\";\n");
        sbJavaScript.append("\tvar cnt=0;\n");
        sbJavaScript.append("\tfor ( var i = 0; i < keyidlistcount; i ++ ) {\n");
        sbJavaScript.append("\t\tif( keyidlist[i].length > 0 ) {\n");
        sbJavaScript.append("\t\t    cnt++;\n");
        sbJavaScript.append("\t\t\ttemp += \"<tr><td nowrap width=10 align=\\\"left\\\"><img style=\\\"cursor:pointer\\\" title=\\\"Click to remove from list\\\" src=\\\"WEB-OPAL/elements/advancedsearch/images/redcross.gif\\\" onClick=\\\"removeManual( '\" + i + \"' );sapphire.events.cancelEvent(event, false);\\\"/> </td>  <td nowrap>\" + keyidlist[i] + \"</td>  </tr>\"\n");
        sbJavaScript.append("\t\t\tfrmScanSearch.keyid1.value += keyidlist[i]\n");
        sbJavaScript.append("\t\t\tfrmScanSearch.forsqlwhere.value += sqlw+keyidlist[i]+sqlw;\n");
        sbJavaScript.append("\t\t\tif ( i + 1 < keyidlistcount ) frmScanSearch.keyid1.value += \"%3B\";\n");
        sbJavaScript.append("\t\t\tif ( i + 1 < keyidlistcount ) frmScanSearch.forsqlwhere.value += \",\";\n");
        sbJavaScript.append("\t\t}\n");
        sbJavaScript.append("\t}\n");
        sbJavaScript.append("\ttemp += \"</table>\";//alert(temp)\n");
        sbJavaScript.append("\tdocument.getElementById( \"manualkeyids\" ).innerHTML = temp;\n");
        sbJavaScript.append("\tif (cnt>0){\n");
        sbJavaScript.append("       document.getElementById('countdiv').innerHTML='(' + cnt + ')';\n");
        sbJavaScript.append("   }\n");
        sbJavaScript.append("   else{\n");
        sbJavaScript.append("       document.getElementById('countdiv').innerHTML='';\n");
        sbJavaScript.append("   }\n");
        sbJavaScript.append("}\n");
        sbJavaScript.append("\n");
        sbJavaScript.append("function addManual() {\n");
        sbJavaScript.append("\tif ( frmScanSearch.manualkeyid.value.length > 0 ) {\n");
        sbJavaScript.append("\t    var arrKeyIds = frmScanSearch.manualkeyid.value.split(\";\");\n");
        sbJavaScript.append("\t    for (var i=0; i< arrKeyIds.length; i++){\n");
        sbJavaScript.append("\t\t    keyidlist[keyidlistcount++] = arrKeyIds[i];\n");
        sbJavaScript.append("\t    }\n");
        sbJavaScript.append("\t\trefreshManual();\n");
        sbJavaScript.append("\t\tfrmScanSearch.manualkeyid.value = \"\";\n");
        sbJavaScript.append("\t\tfrmScanSearch.manualkeyid.focus();\n");
        sbJavaScript.append("\t}\n");
        sbJavaScript.append("}\n");
        sbJavaScript.append("function manualKeyPress() {\n");
        sbJavaScript.append("\tif ( event.keyCode == 13 || event.keyCode == 40 ) {\n");
        sbJavaScript.append("\t\taddManual();\n");
        sbJavaScript.append("\t\tsapphire.events.cancelEvent(event, false);\n");
        sbJavaScript.append("\t}\n");
        sbJavaScript.append("}\n");
        sbJavaScript.append("\n");
        sbJavaScript.append("function removeManual( index ) {\n");
        sbJavaScript.append("\tkeyidlist[index] = \"\";\n");
        sbJavaScript.append("\trefreshManual();\n");
        sbJavaScript.append("}\n");
        sbJavaScript.append("function removeall() {\n");
        sbJavaScript.append("\tfor ( var i = 0; i < keyidlistcount; i ++ ) {\n");
        sbJavaScript.append("\t\tif( keyidlist[i].length > 0 ) {\n");
        sbJavaScript.append("\t        keyidlist[i] = \"\";\n");
        sbJavaScript.append("\t\t}\n");
        sbJavaScript.append("\t}\n");
        sbJavaScript.append("\trefreshManual();\n");
        sbJavaScript.append("}\n");
        sbJavaScript.append("\n");
        sbJavaScript.append("function SearchByScanId() { \n");
        sbJavaScript.append("        if ( current != null ) current.src = null;\n");
        sbJavaScript.append("        if (frmScanSearch.manualkeyid.value.length > 0) addManual();\n");
        sbJavaScript.append("        searchid = document.getElementById('forsqlwhere').value;\n");
        sbJavaScript.append("        if (searchid.substring(searchid.length-1) == ',') searchid = searchid.substring(0,searchid.length-1);\n");
        sbJavaScript.append("        querywhere =\"( " + (String)hmSdcProps.get("tableid") + "." + col + ") IN (\"+searchid+\"  ) \";\n");
        if (this.__Callback.equalsIgnoreCase("")) {
            sbJavaScript.append("    sapphire.alert('" + this.__Tp.translate("Callback javascript function not found in the jsp. Cannot search the list.") + "')\n");
        } else {
            sbJavaScript.append("    if (searchid.length > 0) " + this.__Callback + "( 'basic', searchid, '" + this.__SdcId + "','" + (String)hmSdcProps.get("tableid") + "', querywhere );\n");
        }
        sbJavaScript.append("}\n");
        sbJavaScript.append("</script>\n");
        sdcProc = null;
        hmSdcProps = null;
        return sbJavaScript;
    }
}

