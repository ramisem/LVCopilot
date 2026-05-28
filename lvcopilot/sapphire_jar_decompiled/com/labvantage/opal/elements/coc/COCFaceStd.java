/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.coc;

import com.labvantage.opal.elements.coc.COCFace;
import com.labvantage.opal.elements.coc.SDICOC;
import com.labvantage.sapphire.pageelements.controls.Tab;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import sapphire.accessor.TranslationProcessor;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class COCFaceStd
implements COCFace {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private PropertyList __Element;
    private SDICOC __SdiCoc;
    private List __ColumnsList;
    private List __TitleList;
    private List __WidthList;
    private List __AlignList;
    private List __ModeList;
    private TranslationProcessor __TranslationProcessor;

    public COCFaceStd(PropertyList element, SDICOC sdicoc) {
        this.__Element = element;
        this.__SdiCoc = sdicoc;
        this.__ColumnsList = new ArrayList();
        this.__TitleList = new ArrayList();
        this.__WidthList = new ArrayList();
        this.__AlignList = new ArrayList();
        this.__ModeList = new ArrayList();
    }

    @Override
    public void setElement(PropertyList element) {
        this.__Element = element;
    }

    @Override
    public String getGroupBar() {
        StringBuffer sb = new StringBuffer();
        Set set = this.__SdiCoc.getCustodianSet();
        if (!set.isEmpty()) {
            Iterator iterator = set.iterator();
            int i = 0;
            if (this.__SdiCoc.hasMultipleCustodians()) {
                sb.append("<script>");
                sb.append("    fnResizeWindow( 'groupbar' );");
                sb.append("</script>");
                sb.append("<table id=groupbartable cellspacing='0' cellpadding='5' width=125 border=0>");
                sb.append("<tr>");
                sb.append("<td width='100%' height='22' class='toprowtab' align=center colspan=2>C U S T O D I A N</td>");
                sb.append("</tr>");
                while (iterator.hasNext()) {
                    String custodianid = (String)iterator.next();
                    sb.append("<tr style='cursor: pointer;background:#ccc;' id='_groupbarrow_" + i + "'");
                    sb.append("  onClick=fnSelectionHandler('" + i + "');>");
                    sb.append("<td>");
                    sb.append(custodianid);
                    sb.append("</td><td><img src='WEB-OPAL/images/redrightarrow.gif'");
                    sb.append(" border=0 height=15 style='display:none' id='_bar_img_" + i + "'>");
                    sb.append("</td></tr>");
                    ++i;
                }
                if (this.__SdiCoc.isAnyItemPending()) {
                    sb.append("<tr style='cursor: pointer;background:#ccc;' id='_groupbarrow_" + i + "'");
                    sb.append("  onClick=fnSelectionHandler('" + i + "');>");
                    sb.append("<td>");
                    sb.append("Not Started");
                    sb.append("</td><td><img src='WEB-OPAL/images/redrightarrow.gif'");
                    sb.append(" border=0 height=15 style='display:none' id='_bar_img_" + i + "'>");
                    sb.append("</td></tr>");
                }
                sb.append("</table>");
            } else {
                sb.append("<script>_groupbarvisible = false;</script>");
            }
        } else {
            sb.append("<script>_groupbarvisible = false;</script>");
        }
        return sb.toString();
    }

    @Override
    public String getFromTab() {
        Tab tab = new Tab();
        PropertyList from = this.__Element.getPropertyList("from");
        PropertyList fromtab = from.getPropertyList("tab");
        tab = new Tab();
        String tabtext = fromtab.getProperty("text");
        String width = fromtab.getProperty("width");
        String bodywidth = fromtab.getProperty("bodywidth");
        String bodyheight = fromtab.getProperty("bodyheight");
        String expandable = fromtab.getProperty("expandable");
        String expanded = fromtab.getProperty("expanded");
        String highlight = fromtab.getProperty("highlight");
        String tip = fromtab.getProperty("tip");
        String id = fromtab.getProperty("id");
        if (tabtext == null || tabtext.length() == 0) {
            tabtext = "FROM";
        }
        if (width == null || width.length() == 0) {
            width = "80";
        }
        if (bodywidth == null || bodywidth.length() == 0) {
            bodywidth = "250";
        }
        if (bodyheight == null || bodyheight.length() == 0) {
            bodyheight = "60";
        }
        if (expandable == null || expandable.length() == 0) {
            expandable = "N";
        }
        if (expanded == null || expanded.length() == 0) {
            expanded = "Y";
        }
        if (highlight == null || highlight.length() == 0) {
            highlight = "N";
        }
        if (tip == null || tip.length() == 0) {
            tip = this.__TranslationProcessor.translate("From Custodian");
        }
        if (id == null || id.length() == 0) {
            id = this.__Element.getId() + "_fromtab";
        }
        tab.setText(this.__TranslationProcessor.translate(tabtext));
        tab.setWidth(width);
        tab.setBodywidth(bodywidth);
        tab.setBodyheight(bodyheight);
        tab.setExpandable(expandable.equals("Y") ? "true" : "false");
        tab.setExpanded(expanded.equals("Y") ? "true" : "false");
        tab.setHighlight(highlight.equals("Y") ? "true" : "false");
        tab.setTip(tip);
        tab.setId(id);
        tab.setContent(this.getFromTabHtml());
        tab.setCollapsedtext(this.__TranslationProcessor.translate("Click the tab to show more information."));
        return tab.getHtml();
    }

    private String getFromTabHtml() {
        StringBuffer sb = new StringBuffer();
        PropertyList from = this.__Element.getPropertyList("from");
        PropertyList frombody = from.getPropertyList("body");
        String appearance = frombody.getProperty("appearance");
        String usernametext = frombody.getProperty("usernametext");
        String usernamefieldtype = frombody.getProperty("usernamefieldtype");
        String pwdtext = frombody.getProperty("pwdtext");
        String pwdfieldtype = frombody.getProperty("pwdfieldtype");
        if (appearance == null || appearance.length() == 0) {
            appearance = "blue";
        }
        sb.append("<table cellpadding=1 cellspacing=0 border=0 width=100%>");
        sb.append("<tr><td>");
        sb.append("<table cellpadding=2 cellspacing=0 border=0 width=100%");
        sb.append(" class='maintform_table_" + appearance + "'>");
        sb.append("<tr><td class='maintform_fieldtitle_" + appearance + "'");
        sb.append(" width='50%'>");
        sb.append(this.__TranslationProcessor.translate(usernametext));
        sb.append("</td><td class='maintform_field_" + appearance + "'");
        sb.append(" width='50%'>");
        sb.append(this.getField(usernamefieldtype, "__fromusername"));
        sb.append("</td></tr>");
        sb.append("<tr><td class='maintform_fieldtitle_" + appearance + "'>");
        sb.append(this.__TranslationProcessor.translate(pwdtext));
        sb.append("</td><td class='maintform_field_" + appearance + "'>");
        sb.append(this.getField(pwdfieldtype, "__frompassword"));
        sb.append("</td></tr>");
        sb.append("</table>");
        sb.append("</td></tr>");
        sb.append("</table>");
        return sb.toString();
    }

    @Override
    public String getToTab() {
        Tab tab = new Tab();
        PropertyList to = this.__Element.getPropertyList("to");
        PropertyList totab = to.getPropertyList("tab");
        tab = new Tab();
        String tabtext = totab.getProperty("text");
        String width = totab.getProperty("width");
        String bodywidth = totab.getProperty("bodywidth");
        String bodyheight = totab.getProperty("bodyheight");
        String expandable = totab.getProperty("expandable");
        String expanded = totab.getProperty("expanded");
        String highlight = totab.getProperty("highlight");
        String tip = totab.getProperty("tip");
        String id = totab.getProperty("id");
        if (tabtext == null || tabtext.length() == 0) {
            tabtext = this.__TranslationProcessor.translate("FROM");
        }
        if (width == null || width.length() == 0) {
            width = "80";
        }
        if (bodywidth == null || bodywidth.length() == 0) {
            bodywidth = "250";
        }
        if (bodyheight == null || bodyheight.length() == 0) {
            bodyheight = "60";
        }
        if (expandable == null || expandable.length() == 0) {
            expandable = "N";
        }
        if (expanded == null || expanded.length() == 0) {
            expanded = "Y";
        }
        if (highlight == null || highlight.length() == 0) {
            highlight = "N";
        }
        if (tip == null || tip.length() == 0) {
            tip = this.__TranslationProcessor.translate("From Custodian");
        }
        if (id == null || id.length() == 0) {
            id = this.__Element.getId() + "_totab";
        }
        tab.setText(this.__TranslationProcessor.translate(tabtext));
        tab.setWidth(width);
        tab.setBodywidth(bodywidth);
        tab.setBodyheight(bodyheight);
        tab.setExpandable(expandable.equals("Y") ? "true" : "false");
        tab.setExpanded(expanded.equals("Y") ? "true" : "false");
        tab.setHighlight(highlight.equals("Y") ? "true" : "false");
        tab.setTip(tip);
        tab.setId(id);
        tab.setContent(this.getToTabHtml());
        tab.setCollapsedtext(this.__TranslationProcessor.translate("Click the tab to show more information."));
        return tab.getHtml();
    }

    private String getToTabHtml() {
        StringBuffer sb = new StringBuffer();
        PropertyList to = this.__Element.getPropertyList("to");
        PropertyList tobody = to.getPropertyList("body");
        String appearance = tobody.getProperty("appearance");
        String usernametext = tobody.getProperty("usernametext");
        String usernamefieldtype = tobody.getProperty("usernamefieldtype");
        String pwdtext = tobody.getProperty("pwdtext");
        String pwdfieldtype = tobody.getProperty("pwdfieldtype");
        if (appearance == null || appearance.length() == 0) {
            appearance = "blue";
        }
        sb.append("<table cellpadding=2 cellspacing=0 border=0 width=100%>");
        sb.append("<tr><td>");
        sb.append("<table cellpadding=2 cellspacing=0 border=0 width=100%");
        sb.append(" class='maintform_table_" + appearance + "'>");
        sb.append("<tr><td class='maintform_fieldtitle_" + appearance + "'");
        sb.append(" width='50%'>");
        sb.append(this.__TranslationProcessor.translate(usernametext));
        sb.append("</td><td class='maintform_field_" + appearance + "'");
        sb.append(" width='50%'>");
        sb.append(this.getField(usernamefieldtype, "__tousername"));
        sb.append("</td></tr>");
        sb.append("<tr><td class='maintform_fieldtitle_" + appearance + "'>");
        sb.append(this.__TranslationProcessor.translate(pwdtext));
        sb.append("</td><td class='maintform_field_" + appearance + "'>");
        sb.append(this.getField(pwdfieldtype, "__topassword"));
        sb.append("</td></tr>");
        sb.append("</table>");
        sb.append("</td></tr>");
        sb.append("</table>");
        return sb.toString();
    }

    @Override
    public String getWitnessTab(List certifieduserlist) {
        Tab tab = new Tab();
        PropertyList witness = this.__Element.getPropertyList("witness");
        PropertyList witnesstab = witness.getPropertyList("tab");
        tab = new Tab();
        String tabtext = witnesstab.getProperty("text");
        String width = witnesstab.getProperty("width");
        String bodywidth = witnesstab.getProperty("bodywidth");
        String bodyheight = witnesstab.getProperty("bodyheight");
        String expandable = witnesstab.getProperty("expandable");
        String expanded = witnesstab.getProperty("expanded");
        String highlight = witnesstab.getProperty("highlight");
        String tip = witnesstab.getProperty("tip");
        String id = witnesstab.getProperty("id");
        if (tabtext == null || tabtext.length() == 0) {
            tabtext = this.__TranslationProcessor.translate("WITNESS");
        }
        if (width == null || width.length() == 0) {
            width = "80";
        }
        if (bodywidth == null || bodywidth.length() == 0) {
            bodywidth = "518";
        }
        if (bodyheight == null || bodyheight.length() == 0) {
            bodyheight = "40";
        }
        if (expandable == null || expandable.length() == 0) {
            expandable = "N";
        }
        if (expanded == null || expanded.length() == 0) {
            expanded = "Y";
        }
        if (highlight == null || highlight.length() == 0) {
            highlight = "N";
        }
        if (tip == null || tip.length() == 0) {
            tip = this.__TranslationProcessor.translate("Witness");
        }
        if (id == null || id.length() == 0) {
            id = this.__Element.getId() + "_witnesstab";
        }
        tab.setText(this.__TranslationProcessor.translate(tabtext));
        tab.setWidth(width);
        tab.setBodywidth(bodywidth);
        tab.setBodyheight(bodyheight);
        tab.setExpandable(expandable.equals("Y") ? "true" : "false");
        tab.setExpanded(expanded.equals("Y") ? "true" : "false");
        tab.setHighlight(highlight.equals("Y") ? "true" : "false");
        tab.setTip(tip);
        tab.setId(id);
        tab.setContent(this.getWitnessTabHtml(certifieduserlist));
        tab.setCollapsedtext(this.__TranslationProcessor.translate("Click the tab to show more information."));
        return tab.getHtml();
    }

    private String getWitnessTabHtml(List certifieduserlist) {
        StringBuffer sb = new StringBuffer();
        PropertyList witness = this.__Element.getPropertyList("witness");
        PropertyList witnessbody = witness.getPropertyList("body");
        String appearance = witnessbody.getProperty("appearance");
        String usernametext = witnessbody.getProperty("usernametext");
        String usernamefieldtype = witnessbody.getProperty("usernamefieldtype");
        String pwdtext = witnessbody.getProperty("pwdtext");
        String pwdfieldtype = witnessbody.getProperty("pwdfieldtype");
        if (appearance == null || appearance.length() == 0) {
            appearance = "blue";
        }
        sb.append("<table cellpadding=2 cellspacing=0 border=0 width=100%>");
        sb.append("<tr><td>");
        sb.append("<table cellpadding=2 cellspacing=0 border=0 width=100%");
        sb.append(" class='maintform_table_" + appearance + "'>");
        sb.append("<tr><td class='maintform_fieldtitle_" + appearance + "'");
        sb.append(" width='25%'>");
        sb.append(this.__TranslationProcessor.translate(usernametext));
        sb.append("</td><td class='maintform_field_" + appearance + "'");
        sb.append(" width='25%'>");
        sb.append(this.getWitnessField(usernamefieldtype, "__witnessid", certifieduserlist));
        sb.append("</td>");
        sb.append("<td class='maintform_fieldtitle_" + appearance + "'");
        sb.append(" width='25%'>");
        sb.append(this.__TranslationProcessor.translate(pwdtext));
        sb.append("</td><td class='maintform_field_" + appearance + "'");
        sb.append(" width='25%'>");
        sb.append(this.getField(pwdfieldtype, "__witnesspassword"));
        sb.append("</td></tr>");
        sb.append("</table>");
        sb.append("</td></tr>");
        sb.append("</table>");
        return sb.toString();
    }

    @Override
    public String getDataTab() {
        Tab tab = new Tab();
        PropertyList witness = this.__Element.getPropertyList("data");
        PropertyList witnesstab = witness.getPropertyList("tab");
        tab = new Tab();
        String tabtext = witnesstab.getProperty("text");
        String width = witnesstab.getProperty("width");
        String bodywidth = witnesstab.getProperty("bodywidth");
        String bodyheight = witnesstab.getProperty("bodyheight");
        String expandable = witnesstab.getProperty("expandable");
        String expanded = witnesstab.getProperty("expanded");
        String highlight = witnesstab.getProperty("highlight");
        String tip = witnesstab.getProperty("tip");
        String id = witnesstab.getProperty("id");
        if (tabtext == null || tabtext.length() == 0) {
            tabtext = this.__TranslationProcessor.translate("WITNESS");
        }
        if (width == null || width.length() == 0) {
            width = "80";
        }
        if (bodywidth == null || bodywidth.length() == 0) {
            bodywidth = "518";
        }
        if (bodyheight == null || bodyheight.length() == 0) {
            bodyheight = "40";
        }
        if (expandable == null || expandable.length() == 0) {
            expandable = "N";
        }
        if (expanded == null || expanded.length() == 0) {
            expanded = "Y";
        }
        if (highlight == null || highlight.length() == 0) {
            highlight = "N";
        }
        if (tip == null || tip.length() == 0) {
            tip = this.__TranslationProcessor.translate("Witness");
        }
        if (id == null || id.length() == 0) {
            id = this.__Element.getId() + "_datatab";
        }
        tab.setText(this.__TranslationProcessor.translate(tabtext));
        tab.setWidth(width);
        tab.setBodywidth(bodywidth);
        tab.setBodyheight(bodyheight);
        tab.setExpandable(expandable.equals("Y") ? "true" : "false");
        tab.setExpanded(expanded.equals("Y") ? "true" : "false");
        tab.setHighlight(highlight.equals("Y") ? "true" : "false");
        tab.setTip(tip);
        tab.setId(id);
        tab.setContent(this.getDataTabHtml());
        tab.setCollapsedtext(this.__TranslationProcessor.translate("Click the tab to show more information."));
        return tab.getHtml();
    }

    private String getDataTabHtml() {
        this.parseElementColumns();
        StringBuffer sb = new StringBuffer();
        PropertyList data = this.__Element.getPropertyList("data");
        PropertyList databody = data.getPropertyList("body");
        String appearance = databody.getProperty("appearance");
        if (appearance == null || appearance.length() == 0) {
            appearance = "blue";
        }
        sb.append("<table class='maintform_table_").append(appearance).append("'");
        sb.append(" border=1 cellpadding=2 cellspacing=0 id='" + this.__Element.getId() + "'>");
        sb.append("<thead><tr height=26\">");
        for (int i = 0; i < this.__ColumnsList.size(); ++i) {
            if (this.__ModeList.get(i).equals("hidden")) continue;
            sb.append("<th class='maintform_fieldtitle_").append(appearance);
            sb.append("' align='center' width=");
            sb.append((String)this.__WidthList.get(i)).append(">");
            sb.append((String)this.__TitleList.get(i)).append("</th>");
        }
        sb.append("</tr></thead><tbody></tbody></table>");
        return sb.toString();
    }

    private String getField(String fieldtype, String fieldid) {
        StringBuffer sb = new StringBuffer();
        if (fieldtype.equals("readonly")) {
            sb.append("<input name='_field' id='" + fieldid + "' size=20 style='border:0' READONLY>");
        } else if (fieldtype.equals("input")) {
            sb.append("<input name='_field' id='" + fieldid + "' size=20 onChange='fnModified(this);'>");
        } else if (fieldtype.equals("password")) {
            sb.append("<input name='_field' type='password' id='" + fieldid + "' size=20 onChange='fnModified(this);'>");
        }
        return sb.toString();
    }

    private String getWitnessField(String fieldtype, String fieldid, List certifieduserlist) {
        StringBuffer sb = new StringBuffer();
        if (fieldtype.equals("readonly")) {
            sb.append("<input name='_field' id='" + fieldid + "' size=20 style='border:0' READONLY>");
        } else if (fieldtype.equals("input")) {
            sb.append("<input name='_field' id='" + fieldid + "' size=20 onChange='fnModified(this);'>");
        } else if (fieldtype.equals("dropdownlist")) {
            sb.append("<select name='_field' id='" + fieldid + "' onChange='fnModified(this);'>");
            sb.append("<option>-- Select Witness --</option>");
            for (int i = 0; i < certifieduserlist.size(); ++i) {
                sb.append("<option>" + certifieduserlist.get(i) + "</option>");
            }
            sb.append("</select>");
        }
        return sb.toString();
    }

    private boolean parseElementColumns() {
        PropertyListCollection columns = this.__Element.getPropertyList("data").getPropertyList("body").getCollection("columns");
        if (columns == null) {
            return false;
        }
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList column = columns.getPropertyList(i);
            String columnid = column.getProperty("column");
            String columntitle = column.getProperty("title");
            String columnwidth = column.getProperty("width");
            String columnalign = column.getProperty("align");
            String columnmode = column.getProperty("mode");
            if (columntitle == null || columntitle.length() == 0) {
                columntitle = columnid;
            }
            if (columnwidth == null || columnwidth.length() == 0) {
                columnwidth = "150";
            }
            if (columnalign == null || columnalign.length() == 0) {
                columnalign = "left";
            }
            if (columnmode == null || columnmode.length() == 0) {
                columnmode = "readonly";
            }
            this.__ColumnsList.add(i, columnid);
            this.__TitleList.add(i, columntitle);
            this.__WidthList.add(i, columnwidth);
            this.__AlignList.add(i, columnalign);
            this.__ModeList.add(i, columnmode);
        }
        return true;
    }

    @Override
    public void setTranslationProcessor(TranslationProcessor __TranslationProcessor) {
        this.__TranslationProcessor = __TranslationProcessor;
    }
}

