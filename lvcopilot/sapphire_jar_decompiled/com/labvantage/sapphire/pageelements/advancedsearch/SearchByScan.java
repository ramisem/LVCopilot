/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.advancedsearch;

import com.labvantage.sapphire.pageelements.advancedsearch.SearchContent;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.scrollpanel.ScrollPanel;
import java.util.HashMap;
import sapphire.accessor.SDCProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SearchByScan
extends SearchContent {
    String contentName = "scan";

    @Override
    public String getHtml() {
        String showAddButton = this.contentProperties.getProperty("showaddbutton");
        String showResetButton = this.contentProperties.getProperty("showresetbutton");
        StringBuffer output = new StringBuffer("");
        Button okButton = new Button(this.pageContext);
        Button addButton = new Button(this.pageContext);
        Button clearButton = new Button(this.pageContext);
        okButton.setAction("openScan()");
        okButton.setMargin("none");
        okButton.setAppearance("smalltext");
        okButton.setImg("WEB-CORE/imageref/flat/16/flat_black_search.svg");
        okButton.setAppearance("ribbonsmall");
        okButton.setStyle("width:25px;height:22px; opacity:0.6; border: 1px solid rgba(26,26,26,0.35); border-radius:3px; padding:2px 2px 2px 2px;");
        boolean longAddText = false;
        if (showAddButton.equalsIgnoreCase("") || showAddButton.equalsIgnoreCase("Y")) {
            String addText = this.translator.translate("Add");
            if (addText.length() > 4) {
                longAddText = true;
            }
            addButton.setText(addText);
            addButton.setAppearance("smalltext");
            addButton.setAction("addManual()");
            addButton.setMargin("none");
            addButton.setStyle("font-size:7pt; width:25px; height:22px; opacity:1; border: 1px solid rgba(26,26,26,0.4); border-radius:3px; padding:2px 2px 2px 2px");
        }
        if (showResetButton.equalsIgnoreCase("") || showResetButton.equalsIgnoreCase("Y")) {
            clearButton.setText(this.translator.translate("Clear"));
            clearButton.setAppearance("smalltext");
            clearButton.setAction("removeall()");
            clearButton.setMargin("none");
            clearButton.setStyle("font-size: 7px; width:30px; height:22px; opacity:1; border: 1px solid rgba(26,26,26,0.4); border-radius:3px; padding:2px 2px 2px 2px");
        }
        output.append("<form name=\"frmScanSearch\" id=\"frmScanSearch\" style=\"padding:0px; margin:0px;\">");
        output.append("<input type=\"hidden\" name=\"keyid1\" id=\"keyid1\" value=\"\">");
        output.append("<input type=\"hidden\" name=\"forsqlwhere\" id=\"forsqlwhere\" value=\"\">");
        output.append("<table class=\"search_table\" style=\"padding:2px;border:0;background-color:transparent\" cellpadding=\"0\" cellspacing=\"1\">");
        output.append("<tr>");
        output.append("<td style=\"vertical-align:middle;width:8px\"><img style=\"visibility: hidden;margin-right:2px\" id=\"scanpointer\" src=\"WEB-CORE/elements/images/selected_item.gif\"></td>");
        output.append(" <td>");
        output.append("<input type=\"text\" class=\"modern_search_scanfield\" style=\"vertical-align:middle;\" name=\"manualkeyid\" id=\"manualkeyid\" value=\"\" onKeyDown=\"manualKeyPress()\">");
        output.append("</td>");
        if (showAddButton.equalsIgnoreCase("") || showAddButton.equalsIgnoreCase("Y")) {
            output.append("<td>").append(addButton.getHtml()).append("</td>");
        }
        if (longAddText) {
            output.append("<td>&nbsp;</td></tr><tr><td>&nbsp;</td><td>&nbsp;</td><td>").append(okButton.getHtml()).append("</td><td>&nbsp;</td>");
        } else {
            output.append("<td>").append(okButton.getHtml()).append("</td>");
        }
        output.append("<td style=\"width:100%\">&nbsp;</td>");
        output.append("</tr></table>");
        output.append("<div id=\"manualkeyids\" style=\"margin:2px; width:100%\"/></div>");
        if (showResetButton.equalsIgnoreCase("") || showResetButton.equalsIgnoreCase("Y")) {
            output.append("<div style=\"padding-left:125px\">").append(clearButton.getHtml()).append("</div>");
        }
        output.append("</form>");
        output.append(this.getJavaScript());
        if (this.maxHeight > 0) {
            ScrollPanel scrollPanel = new ScrollPanel(this.pageContext);
            scrollPanel.setId(this.contentName);
            scrollPanel.setMaxHeight(this.maxHeight);
            scrollPanel.setModernScroll(true);
            scrollPanel.setContent(output);
            return scrollPanel.getHtml();
        }
        return output.toString();
    }

    private StringBuffer getJavaScript() {
        StringBuffer output = new StringBuffer();
        HashMap sdcProps = new SDCProcessor(this.pageContext).getSDCProperties(this.sdcid);
        String matchColumn = this.contentProperties.getProperty("matchcolumn");
        String col = "";
        String keycolid1 = (String)sdcProps.get("keycolid1");
        if (matchColumn != null && matchColumn.length() > 0) {
            col = matchColumn;
            if (matchColumn.equals(keycolid1)) {
                matchColumn = "";
            }
        } else {
            col = keycolid1;
        }
        RequestContext requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
        PropertyList userConfig = requestContext.getPropertyList("userconfig");
        String scanlist = this.isLastSearchType ? userConfig.getProperty("as_searchid_" + this.cookieKey) : "";
        String preservescanorder = this.contentProperties.getProperty("preservescanorder");
        if (preservescanorder == null) {
            preservescanorder = "";
        }
        output.append("<script language=\"JavaScript\">").append("var keyidlist = new Array();\n").append("var keyidlistcount = 0;\n").append("function refreshManual() {\n").append("\tfrmScanSearch.keyid1.value = \"\";\n").append("\tfrmScanSearch.forsqlwhere.value = \"\";\n").append("\tvar\ttemp = \"<table border='0' width=100% class=\\\"search_scan_text\\\">\";\n").append("\tvar cnt=0;\n").append("\tfor ( var i = 0; i < keyidlistcount; i ++ ) {\n").append("\t\tif( keyidlist[i].length > 0 ) {\n").append("\t\t    cnt++;\n").append("\t\t\ttemp += \"<tr><td>&nbsp;</td><td nowrap>\" + keyidlist[i] + \"</td><td>&nbsp;</td><td nowrap width=10 align=\\\"left\\\"><img style=\\\"cursor:pointer\\\" class=\\\"search_scan_remove_img\\\" title=\\\"Click to remove from list\\\" src=\\\"WEB-CORE/imageref/flat/16/flat_black_close_remove2.svg\\\" onClick=\\\"removeManual( '\" + i + \"' );sapphire.events.cancelEvent(event, false);\\\"/> </td> </tr>\"\n").append("\t\t\tfrmScanSearch.keyid1.value += \"%3B\" + keyidlist[i]\n").append("\t\t\tfrmScanSearch.forsqlwhere.value += \",'\" + keyidlist[i] + \"'\";\n").append("\t\t}\n").append("\t}\n").append("  if ( frmScanSearch.keyid1.value.length > 0 ) frmScanSearch.keyid1.value = frmScanSearch.keyid1.value.substring( 3 )\n").append("  if ( frmScanSearch.forsqlwhere.value.length > 0 ) frmScanSearch.forsqlwhere.value = frmScanSearch.forsqlwhere.value.substring( 1 )\n").append("\ttemp += \"</table>\";//alert(temp)\n").append("\tdocument.getElementById( \"manualkeyids\" ).innerHTML = temp;\n").append("\tif (cnt>0){\n").append("       document.getElementById('countdiv').innerHTML='(' + cnt + ')';\n").append("   }\n").append("   else{\n").append("       document.getElementById('countdiv').innerHTML='';\n").append("   }\n");
        if (this.maxHeight > 0) {
            output.append(" if (typeof(__scrolldiv_").append(this.contentName).append(") !='undefined'){").append("sapphire.ui.util.scrollTo(__scrolldiv_").append(this.contentName).append(",null,").append(this.maxHeight).append(");}\n");
        }
        output.append("}\n").append("function addManual() {\n").append("\tif ( frmScanSearch.manualkeyid.value.length > 0 ) {\n").append("\t    var arrKeyIds = frmScanSearch.manualkeyid.value.split(\";\");\n").append("\t    for (var i=0; i< arrKeyIds.length; i++){\n").append("\t\t    keyidlist[keyidlistcount++] = arrKeyIds[i].replace(/^\\s*|\\s*$/g,\"\");\n").append("\t    }\n").append("\t\trefreshManual();\n").append("\t\tfrmScanSearch.manualkeyid.value = \"\";\n").append("\t\tfrmScanSearch.manualkeyid.focus();\n").append("\t\tif (typeof(updateSearchBarScroll)!='undefined'){updateSearchBarScroll();};\n").append("\t}\n").append("}\n").append("function manualKeyPress() {\n").append("\tif ( event.keyCode == 13 || event.keyCode == 40 ) {\n").append("\t\taddManual();\n").append("\t\tsapphire.events.cancelEvent(event, false);\n").append("\t}\n").append("}\n").append("\n").append("function removeManual( index ) {\n").append("\tkeyidlist[index] = \"\";\n").append("\trefreshManual();\n").append("}\n").append("function removeall() {\n").append("\tfor ( var i = 0; i < keyidlistcount; i ++ ) {\n").append("\t\tif( keyidlist[i].length > 0 ) {\n").append("\t        keyidlist[i] = \"\";\n").append("\t\t}\n").append("\t}\n").append("\trefreshManual();\n").append("}\n").append("function openScan() { \n").append("  if (frmScanSearch.manualkeyid.value.length > 0) addManual();\n").append("  var xsearchid = document.getElementById('keyid1').value;\n").append("  var searchid = document.getElementById('keyid1').value;\n").append("  sapphire.userConfig.set( \"as_searchid_").append(this.cookieKey).append("\",  searchid )\n;").append("  var itemlist = document.getElementById('forsqlwhere').value;\n").append("  querywhere =\"( ").append((String)sdcProps.get("tableid")).append(".").append(col).append(") IN (\"+").append("itemlist").append("+\"  ) \";\n").append("  if (itemlist.length > 0) {\n").append("    document.getElementById( \"scanpointer\" ).style.visibility='visible';\n").append(matchColumn.length() > 0 ? " doCallback( 'scan', itemlist, '" + this.sdcid + "', '', '', '', frmScanSearch.keyid1.value, '" + preservescanorder + "', '" + col + "' )\n" : "    doCallback( 'scan', itemlist, '" + this.sdcid + "','', '', '', frmScanSearch.keyid1.value, '" + preservescanorder + "' )\n").append("  }\n").append("}\n").append("function hideScanPointer() {\n").append("  document.getElementById( \"scanpointer\" ).style.visibility='hidden';\n").append("}\n");
        if (scanlist.length() > 0) {
            if (scanlist.indexOf(";") > -1) {
                scanlist = scanlist.replaceAll(";", "%3B");
            }
            String[] items = StringUtil.split(scanlist, "%3B");
            for (int i = 0; i < items.length; ++i) {
                output.append("keyidlist[").append(i).append("] = '").append(SafeHTML.encodeForJavaScript(items[i])).append("';\n");
            }
            output.append("keyidlistcount=").append(items.length).append(";\n");
            output.append("refreshManual();\n");
            output.append("sapphire.events.attachEvent( window, 'onload', openScan );\n");
        } else {
            output.append("removeall();\n");
        }
        output.append("</script>");
        return output;
    }
}

