/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.layouts;

import com.labvantage.sapphire.pageelements.controls.Button;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.TranslationProcessor;
import sapphire.xml.PropertyList;

public class Favorite {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private PageContext pageContext;
    private PropertyList pageData;
    private TranslationProcessor translator = null;

    public void init(PageContext pagecontext, PropertyList pageData) {
        this.pageContext = pagecontext;
        this.pageData = pageData;
        this.translator = new TranslationProcessor(this.pageContext);
    }

    public String getAddButton() {
        Button addButton = new Button(this.pageContext);
        addButton.setText(this.translator.translate("Add"));
        addButton.setAction("favoritesiframe.executeProcess( 'AddFavorite' );");
        addButton.setImg("WEB-CORE/images/gif/Add.gif");
        addButton.setMargin("narrow");
        addButton.setWidth("70");
        return addButton.getHtml();
    }

    public String showContent() {
        StringBuffer output = new StringBuffer();
        output.append("<style>\n.menuholder\t{ cursor: default; width: 100px; background-color: white; border:black solid 1px }\n.menu\t\t\t{ width: 100%; padding: 2px}\n.menuselected\t{ width: 100%; padding: 2px; background-color: darkblue; color: white }\n.menusep1\t\t{ font-size: 1; height: 3px; border-bottom: gray solid 1px}\n.menusep2\t\t{ font-size: 1; height: 3px; border-top: white solid 1px}\n.favoritetoolbarbutton\t\t{ border: 1px solid black }\n</style>\n");
        output.append("<div id=\"favoritemenu\" class=\"menuholder\" style=\"height: 80px; position: absolute; display:none\">");
        output.append("<div id=\"favoritemenurename\" class=\"menu\" onclick=\"renameFavorite()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\">&nbsp;Rename...</div>");
        output.append("<div id=\"favoritemenudelete\" class=\"menu\" onclick=\"deleteFavorite()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\">&nbsp;Delete...</div>");
        output.append("<div class=\"menusep1\" id=\"sep1_1\"></div>");
        output.append("<div class=\"menusep2\" id=\"sep1_2\"></div>");
        output.append("<div id=\"favoritemenumovetotop\" class=\"menu\" onclick=\"moveFavoriteTop()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\">&nbsp;Move to top...</div>");
        output.append("<div nowrap id=\"favoritemenumovetobottom\" class=\"menu\" onclick=\"moveFavoriteBottom()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\">&nbsp;Move to bottom...</div>");
        output.append("</div>");
        output.append("<iframe id=\"favoritesiframe\" name=\"favoritesiframe\" style=\"display: none\" src='WEB-CORE/blank.html'></iframe>");
        output.append(this.getToolbar());
        output.append("\n<form id=\"favoritesform\" target=\"favoritesiframe\" method=\"post\" action=\"rc?command=file&file=WEB-OPAL/layouts/generic/favorite.jsp\">");
        output.append("<input type=\"hidden\" name=\"favoriteoperation\" value='' />");
        output.append("<input type=\"hidden\" name=\"selecteditem\" value='' />");
        output.append("<input type=\"hidden\" name=\"favoriteurl\" value='" + this.pageData.getProperty("page") + "' />");
        output.append("<input type=\"hidden\" name=\"favoritepageid\" value='" + this.pageData.getProperty("webpageid") + "' />");
        output.append("<input type=\"hidden\" name=\"favoritecommand\" value='" + this.pageData.getProperty("command") + "' />");
        output.append("<input type=\"hidden\" name=\"process\" value='' />");
        output.append("</form>");
        output.append("\n<script>document.getElementById( 'favoritesform' ).submit();</script>");
        return output.toString();
    }

    private StringBuffer getToolbar() {
        StringBuffer output = new StringBuffer();
        String[] action = new String[]{"AddFavorite", "DeleteFavorite", "MoveTop", "MoveBottom"};
        String[] image = new String[]{"newfavorite", "deletefavorite", "movefavoriteup", "movefavoritedown"};
        String[] tip = new String[]{this.translator.translate("Create a new Favorite"), this.translator.translate("Delete the Favorite"), this.translator.translate("Move the Favorite to the top of the list"), this.translator.translate("Move the Favorite to the bottom of the list")};
        Button toolbarButton = new Button(this.pageContext);
        toolbarButton.setMargin("narrow");
        output.append("<table ><tr>");
        for (int i = 0; i < action.length; ++i) {
            toolbarButton.setAction("favoritesiframe.executeProcess( '" + action[i] + "' )");
            toolbarButton.setImg("WEB-OPAL/layouts/generic/images/" + image[i] + ".gif");
            toolbarButton.setTip(tip[i]);
            output.append("<td align=\"center\">" + toolbarButton.getHtml() + "</td>");
        }
        output.append("</tr></table>");
        return output;
    }
}

