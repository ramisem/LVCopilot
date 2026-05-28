/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.advancedsearch;

import com.labvantage.sapphire.pageelements.advancedsearch.SearchContent;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.scrollpanel.ScrollPanel;
import sapphire.accessor.SDCProcessor;
import sapphire.servlet.RequestContext;
import sapphire.xml.PropertyList;

public class SearchByFolder
extends SearchContent {
    String contentName = "folder";

    @Override
    public String getHtml() {
        StringBuffer output = new StringBuffer();
        Button addButton = new Button(this.pageContext);
        addButton.setText(this.translator.translate("Add"));
        addButton.setAction("foldercontentsiframe.executeCommand( 'AddItems' )");
        addButton.setMargin("none");
        addButton.setAppearance("ribbonsmall");
        addButton.setStyle("width:40px; height:22px; opacity:1; border: 1px solid rgba(26,26,26,0.4); border-radius:3px; padding:2px 2px 2px 2px");
        Button removeButton = new Button(this.pageContext);
        removeButton.setText(this.translator.translate("Remove"));
        removeButton.setId("RemoveFromFolderButton");
        removeButton.setAction("foldercontentsiframe.executeCommand( 'RemoveItems' )");
        removeButton.setMargin("none");
        removeButton.setAppearance("ribbonsmall");
        removeButton.setStyle("width:60px; height:22px; opacity:1; border: 1px solid rgba(26,26,26,0.4); border-radius:3px; padding:2px 2px 2px 2px");
        output.append("<div>");
        output.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/advancedsearch/scripts/folder.js\"></script>");
        output.append("<style>\n.menuholder\t{ cursor: default; width: 100px; background-color: white; border:black solid 1px }\n.menu\t\t\t{ width: 100%; padding:2px}\n.menuselected\t{ width: 100%; padding:2px; background-color: darkblue; color: white }\n.menusep1\t\t{ font-size: 1; height:3px; border-bottom: gray solid 1px}\n.menusep2\t\t{ font-size: 1; height:3px; border-top: white solid 1px}\n.foldertoolbarbutton\t\t{ border: 1px solid black }\n</style>\n");
        output.append("<table width=\"80%\" border=0><tr>");
        output.append("<td>").append(addButton.getHtml()).append("</td>");
        output.append("<td>").append("&nbsp;&nbsp;").append("</td>");
        output.append("<td align=right>").append(removeButton.getHtml()).append("</td>");
        output.append("</tr></table>");
        output.append("<div id=\"foldercontents\"></div>");
        output.append("<div id=\"foldermenu\" class=\"menuholder\" style=\"height: 80px; position: absolute; display:none\">");
        output.append("<div id=\"foldermenurename\" class=\"menu\" onclick=\"renameFolder()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\">&nbsp;Rename...</div>");
        output.append("<div id=\"foldermenudelete\" class=\"menu\" onclick=\"deleteFolder()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\">&nbsp;Delete...</div>");
        output.append("<div class=\"menusep1\" id=\"sep1_1\"></div>");
        output.append("<div class=\"menusep2\" id=\"sep1_2\"></div>");
        output.append("<div id=\"foldermenumovetotop\" class=\"menu\" onclick=\"moveFolderTop()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\">&nbsp;Move to top...</div>");
        output.append("<div nowrap id=\"foldermenumovetobottom\" class=\"menu\" onclick=\"moveFolderBottom()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\">&nbsp;Move to bottom...</div>");
        output.append("</div>");
        output.append("<iframe id=\"foldercontentsiframe\" name=\"foldercontentsiframe\"  style=\"display: none\" src='").append(this.browser.getBlankSrc()).append("'");
        if (this.maxHeight > 0) {
            // empty if block
        }
        output.append("></iframe>");
        output.append(this.getToolbar());
        RequestContext requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
        PropertyList userConfig = requestContext.getPropertyList("userconfig");
        output.append("<form id=\"foldercontentsform\" target=\"foldercontentsiframe\" method=\"post\" action=\"rc?command=file&file=WEB-CORE/elements/advancedsearch/searchbyfolder.jsp\">");
        output.append("<input type=\"hidden\" name=\"sdcid\" value=\"").append(this.sdcid).append("\"/>");
        output.append("<input type=\"hidden\" name=\"islastsearchtype\" value=\"").append(this.isLastSearchType ? "Y" : "N").append("\"/>");
        output.append("<input type=\"hidden\" name=\"lastfolderid\" value=\"").append(this.isLastSearchType ? userConfig.getProperty("as_searchid_" + this.cookieKey) : "").append("\"/>");
        output.append("<input type=\"hidden\" name=\"newlabelformat\" value=\"").append(this.contentProperties.getProperty("newlabelformat")).append("\"/>");
        output.append("</form>");
        output.append(this.getJavascript());
        output.append("</div>");
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

    private StringBuffer getJavascript() {
        StringBuffer output = new StringBuffer();
        SDCProcessor sdcProcessor = new SDCProcessor(this.pageContext);
        String tableid = sdcProcessor.getProperty(this.sdcid, "tableid");
        String keycolid1 = sdcProcessor.getProperty(this.sdcid, "keycolid1");
        String keycolid2 = sdcProcessor.getProperty(this.sdcid, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(this.sdcid, "keycolid3");
        output.append("<script>\n");
        output.append("var lastfolderindex = -1;\n");
        output.append("function openFolder( index ) {\n");
        output.append("  var folderelement = document.getElementById( 'folderchoice' + index );\n");
        output.append("  if ( folderelement != null ) {\n");
        output.append("    var folderid = folderelement.getAttribute( 'folderid' );\n");
        output.append("    sapphire.userConfig.set( \"as_searchid_").append(this.cookieKey).append("\",  folderid )\n;");
        output.append("    if ( lastfolderindex >= 0 && lastfolderindex != index ) {\n");
        output.append("      hideFolderPointer();\n");
        output.append("    }");
        output.append("    lastfolderindex = index;\n");
        output.append("    document.getElementById( \"folderchoice\" + index ).checked = true;\n");
        output.append("    document.getElementById( \"folderpointeron\" + index ).style.display='block';\n");
        output.append("    document.getElementById( \"folderpointeroff\" + index ).style.display='none';\n");
        output.append("    document.getElementById( \"RemoveFromFolderButton\" ).style.visibility = 'visible';\n");
        output.append("    document.getElementById( \"folder\" + index ).style.backgroundColor='#eff6fe';document.getElementById( \"folder\" + index ).style.border='1px solid #43a2d6';\n");
        output.append("    doCallback( \"folder\", \"hithere\", \"").append(this.sdcid).append("\", \"").append(tableid).append(", sysuserfolderitem sufi\",\n");
        output.append("      \"sufi.sysuserid='\" + sapphire.connection.sysUserId + \"' AND sufi.sysuserfolderid='\" + folderid + \"' AND sufi.linksdcid='").append(this.sdcid).append("' AND ").append(tableid).append(".").append(keycolid1).append(" = sufi.linkkeyid1");
        if (keycolid2.length() > 0) {
            output.append(" and ").append(tableid).append(".").append(keycolid2).append(" = sufi.linkkeyid2");
        }
        if (keycolid3.length() > 0) {
            output.append(" and ").append(tableid).append(".").append(keycolid3).append(" = sufi.linkkeyid3");
        }
        output.append("\" );\n");
        output.append("  }\n");
        output.append("}\n");
        output.append("var postopenfolderindex=\"\";\n");
        output.append("function doPostOpenFolder() {\n");
        output.append("  openFolder( postopenfolderindex );\n");
        output.append("}\n");
        output.append("function postOpenFolder( index ) {\n");
        output.append("  postopenfolderindex = index;\n");
        output.append("  if ( document.readyState == \"interactive\" ) { \n");
        output.append("    sapphire.events.attachEvent( window, 'onload', doPostOpenFolder );\n ");
        output.append("  }\n ");
        output.append("  else {\n ");
        output.append("    openFolder( index );\n");
        output.append("  }\n ");
        output.append("}\n");
        output.append("function hideFolderPointer() {\n");
        output.append("  if ( lastfolderindex >= 0 ) {\n");
        output.append("    var pointeron = document.getElementById( \"folderpointeron\" +  lastfolderindex );\n");
        output.append("    var pointeroff = document.getElementById( \"folderpointeroff\" +  lastfolderindex );\n");
        output.append("    if ( pointeron != null ) pointeron.style.display='none';\n");
        output.append("    if ( pointeroff != null ) pointeroff.style.display='block';\n");
        output.append("    document.getElementById( \"folder\" + lastfolderindex ).style.backgroundColor='';document.getElementById( \"folder\" + lastfolderindex ).style.border='';\n");
        output.append("  }\n");
        output.append("  document.getElementById( \"RemoveFromFolderButton\" ).style.visibility = 'hidden';\n");
        output.append("  lastfolderindex = -1;\n");
        output.append("}\n");
        output.append("function runFirstFolder() {\n");
        output.append("  openFolder( 0 );\n");
        output.append("}\n");
        output.append("function logError( message ) {\n");
        output.append("  sapphire.alert( \"Error: \" + message );\n");
        output.append("}\n");
        output.append("sapphire.events.attachEvent(window, 'onload', function(){");
        output.append("var o = document.getElementById('foldercontentsform');");
        output.append("if (o != null){");
        output.append("o.submit();");
        output.append("}");
        output.append("});");
        output.append("</script>");
        return output;
    }

    private StringBuffer getToolbar() {
        StringBuffer output = new StringBuffer();
        if (this.contentProperties.getProperty("operationstyle").equalsIgnoreCase("dropdown")) {
            Button okButton = new Button(this.pageContext);
            okButton.setImg("WEB-CORE/imageref/flat/16/flat_black_search.svg");
            okButton.setAppearance("ribbonsmall");
            okButton.setStyle("height:22px;width:25px; border: 1px solid rgba(26,26,26, 0.4); opacity:0.6; border-radius:3px; padding:2px 2px 2px 2px;");
            okButton.setAction("foldercontentsiframe.executeCommand( folderoperation.value )");
            okButton.setMargin("narrow");
            output.append("<table><tr>");
            output.append("<td>or <select id=\"folderoperation\" class=\"modern_search_query_field\">");
            output.append("<option value=\"reset\">").append(this.translator.translate("Choose...")).append("</option>");
            output.append("<option >------------------</option>");
            output.append("<option value=\"AddFolder\">").append(this.translator.translate("New folder")).append("</option>");
            output.append("<option value=\"EmptyFolder\">").append(this.translator.translate("Empty folder")).append("</option>");
            output.append("<option value=\"RenameFolder\">").append(this.translator.translate("Rename folder")).append("</option>");
            output.append("<option value=\"DeleteFolder\">").append(this.translator.translate("Delete folder")).append("</option>");
            output.append("<option >------------------</option>");
            output.append("<option value=\"MoveTop\">").append(this.translator.translate("Move to Top")).append("</option>");
            output.append("<option value=\"MoveBottom\">").append(this.translator.translate("Move to Bottom")).append("</option>");
            output.append("</select></td>");
            output.append("<td>").append(okButton.getHtml()).append("</td>");
            output.append("</tr></table>");
        } else {
            String[] action = new String[]{"AddFolder", "EmptyFolder", "RenameFolder", "DeleteFolder", "MoveTop", "MoveBottom"};
            String[] image = new String[]{"flat_black_plus1", "flat_black_eraser", "flat_black_font_edit", "flat_black_close_remove2", "flat_black_arrow4_up", "flat_black_arrow4_down"};
            String[] tip = new String[]{this.translator.translate("Create a new folder"), this.translator.translate("Empty the folder"), this.translator.translate("Give the folder a new name"), this.translator.translate("Delete the folder"), this.translator.translate("Move the folder to the top of the list"), this.translator.translate("Move the folder to the bottom of the list")};
            Button toolbarButton = new Button(this.pageContext);
            toolbarButton.setMargin("none");
            toolbarButton.setStyle("height:22px; width:25px; border: 1px solid rgba(26,26,26, 0.4); opacity:0.8; border-radius:3px; padding:2px 2px 2px 2px;");
            if (this.browser.getOS() == 14) {
                toolbarButton.setAppearance("ribbonsmall");
            } else {
                toolbarButton.setAppearance("ribbonsmall");
            }
            output.append("<table ><tr>");
            for (int i = 0; i < action.length; ++i) {
                toolbarButton.setAction("foldercontentsiframe.executeCommand( '" + action[i] + "' )");
                toolbarButton.setImg("WEB-CORE/imageref/flat/16/" + image[i] + ".svg");
                toolbarButton.setTip(tip[i]);
                output.append("<td align=\"center\">").append(toolbarButton.getHtml()).append("</td>");
            }
            output.append("</tr></table>");
        }
        return output;
    }
}

