/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.elements.advancedsearch;

import com.labvantage.sapphire.Trace;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.TranslationProcessor;

public class SearchBarDragDropHtml {
    private String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private static final boolean __Debug = false;

    public static String addDragDropSupport(String html, String showSideBar, String showInitially, String refElement, String container, String height, String width, String top, String left, PageContext pageContext) {
        StringBuffer sbHtml = new StringBuffer("");
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        Trace.logDebug("\nOPAL_INFO: SearchBarDragDropHtml.addDragDropSupport()-> About to Add the dragdrop code");
        sbHtml.append("\n<!-- START DRAG DROP SUPPORT -->\n");
        sbHtml.append("<script language=javascript src=\"WEB-OPAL/elements/advancedsearch/scripts/draganddropfunctions.js\"></script>\n");
        sbHtml.append("<script>\n");
        if (showSideBar.equalsIgnoreCase("") || showSideBar.equalsIgnoreCase("N")) {
            sbHtml.append(" showSideBar = false;\n");
        } else {
            sbHtml.append(" showSideBar = true;\n");
        }
        if (!refElement.equalsIgnoreCase("")) {
            sbHtml.append(" referenceelement = '" + refElement + "';\n");
        }
        if (!container.equalsIgnoreCase("")) {
            sbHtml.append(" searchbarcontainer = '" + container + "';\n");
        }
        if (!height.equalsIgnoreCase("")) {
            sbHtml.append(" initialheight = " + height + ";\n");
        }
        if (!width.equalsIgnoreCase("")) {
            sbHtml.append(" initialwidth = " + width + ";\n");
        }
        if (!top.equalsIgnoreCase("")) {
            sbHtml.append(" initialtop = " + top + ";\n");
        }
        if (!left.equalsIgnoreCase("")) {
            sbHtml.append(" left = " + left + ";\n");
        }
        sbHtml.append("</script>\n");
        sbHtml.append("\n<div id=\"dSearchBarContainer\" class=\"dSearchBarContainer\" >\n");
        sbHtml.append("    <table id=searchBarWrapper cellspacing=0 cellpadding=0 height=100% width=100% class=searchBarWrapper><tr><td>\n");
        sbHtml.append("        <div align=\"right\" id=\"dSearchBarHeader\" class=\"dSearchBarHeader\" title=\"" + tp.translate("Click and Hold to drag the Search Bar") + "\" onMousedown=\"initializedrag(event)\" onMouseup=\"stopdrag()\" onSelectStart=\"return false\">\n");
        sbHtml.append("            <table cellspacing=0 cellpadding=0 border=0 width=100%>\n");
        sbHtml.append("            <tr>\n");
        sbHtml.append("                 <td align=left class=searchBarHeading>" + tp.translate("Search Bar") + "</td>\n");
        sbHtml.append("                 <td align=right>\n");
        sbHtml.append("                     <a href=\"\\" + tp.translate("Dock/Undock the Search Bar") + "\" onclick=\"toggleDocking('" + container + "'); return false;\"  >\n");
        sbHtml.append("                         <img id=dockimg title=\"" + tp.translate("Dock/Undock the Search Bar") + "\" border=0 height=16 src=\"WEB-OPAL/elements/advancedsearch/images/pushpin.gif\">\n");
        sbHtml.append("                     </a>\n");
        sbHtml.append("                     <img title=\"" + tp.translate("Maximise/minimise the Search Bar") + "\" src=\"WEB-OPAL/elements/advancedsearch/images/restore.gif\" id=\"maximini\" onClick=\"toggleMaxiMiniFloating(!minimised,true)\">\n");
        sbHtml.append("                     <img title=\"" + tp.translate("Close the Search Bar") + "\" src=\"WEB-OPAL/elements/advancedsearch/images/close.gif\" onClick=\"toggleSearch(null, null)\">\n");
        sbHtml.append("                 </td>\n");
        sbHtml.append("            </tr>\n");
        sbHtml.append("            </table>\n");
        sbHtml.append("        </div>\n");
        sbHtml.append("        <div id=\"dSearchBarContent\" class=\"dSearchBarContent\">\n");
        sbHtml.append("        \n<!-- START SEARCH BAR CONTENT -->\n");
        sbHtml.append(html);
        sbHtml.append("        \n<!-- END SEARCH BAR CONTENT -->\n");
        sbHtml.append("        </div>\n");
        sbHtml.append("    </td></tr></table>\n");
        sbHtml.append("</div>\n");
        sbHtml.append("<script>\n");
        sbHtml.append("    try{\n");
        if (showInitially.equalsIgnoreCase("") || showInitially.equalsIgnoreCase("Y")) {
            sbHtml.append("        sapphire.events.attachEvent( window, \"onload\", showit);\n");
        } else {
            sbHtml.append("        sapphire.events.attachEvent( window, \"onload\", closeit);\n");
        }
        sbHtml.append("    }catch(err){}\n");
        sbHtml.append("</script>\n");
        sbHtml.append("<!-- END DRAG DROP SUPPORT -->\n\n");
        return sbHtml.toString();
    }
}

