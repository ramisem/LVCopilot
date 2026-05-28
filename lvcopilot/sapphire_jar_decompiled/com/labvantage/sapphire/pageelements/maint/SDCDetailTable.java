/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.services.DDTConstants;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class SDCDetailTable
extends BaseElement
implements DDTConstants {
    @Override
    public String getHtml() {
        int i;
        StringBuffer html = new StringBuffer();
        QueryProcessor qp = new QueryProcessor(this.pageContext);
        ConfigurationProcessor cp = new ConfigurationProcessor(this.pageContext);
        TranslationProcessor tp = new TranslationProcessor(this.pageContext);
        boolean isDevMode = false;
        String compCode = "";
        try {
            isDevMode = "Y".equals(cp.getSysConfigProperty("devmode"));
            compCode = isDevMode ? "" : cp.getSysConfigProperty("compcode", "_noCompCode_");
        }
        catch (SapphireException sapphireException) {
            // empty catch block
        }
        DataSet primary = this.sdiInfo.getSDIData().getDataset("primary");
        String sdcid = primary.getValue(0, "sdcid");
        String sdcCompCode = primary.getValue(0, "compcode");
        SafeSQL safeSQL = new SafeSQL();
        DataSet primaryTable = qp.getPreparedSqlDataSet("SELECT tabledoc, tablelabel, itemdisplay FROM systable WHERE tableid = " + safeSQL.addVar(primary.getValue(0, "tableid")), safeSQL.getValues());
        safeSQL.reset();
        DataSet detailTables = qp.getPreparedSqlDataSet("SELECT sdc.tableid \"parenttableid\", linktableid, linkid, linktype, tabletype, linksequence, tabledoc, tablelabel, itemdisplay FROM sdc, sdclink, systable WHERE sdc.sdcid = sdclink.sdcid AND sdclink.linktableid = systable.tableid AND sdc.sdcid = " + safeSQL.addVar(sdcid) + " AND linktype IN ( 'D', 'M' ) UNION SELECT parenttableid, linktableid, linkid, linktype, tabletype, linksequence, tabledoc, tablelabel, itemdisplay FROM sdcdetaillink, systable WHERE sdcdetaillink.linktableid = systable.tableid AND sdcid = " + safeSQL.addVar(sdcid) + " AND linktype IN ( 'D', 'M' ) ORDER BY linksequence", safeSQL.getValues());
        html.append("<table id=\"tables_table\" class=\"maintform_table\" border = \"1\" cellspacing = \"0\" cellpadding = \"3\" width=\"100%\">");
        html.append("<tr>");
        if (isDevMode) {
            html.append("<td class=\"maintform_fieldtitle\"><b>&nbsp;</b></td>");
        }
        html.append("<td class=\"maintform_fieldtitle\" width=\"10\"><b>&nbsp;</b></td>");
        html.append("<td class=\"maintform_fieldtitle\" width=\"150\"><b>" + tp.translate("Table") + "</b></td>");
        html.append("<td class=\"maintform_fieldtitle\" width=\"80\"><b>" + tp.translate("Relation") + "</b></td>");
        html.append("<td class=\"maintform_fieldtitle\" width=\"100\"><b>" + tp.translate("Parent") + "</b></td>");
        html.append("<td class=\"maintform_fieldtitle\" width=\"50\"><b>" + tp.translate("Type") + "</b></td>");
        html.append("<td class=\"maintform_fieldtitle\" width=\"100\"><b>" + tp.translate("Link Id") + "</b></td>");
        html.append("<td class=\"maintform_fieldtitle\" width=\"200\"><b>" + tp.translate("Table Label") + "</b></td>");
        html.append("<td class=\"maintform_fieldtitle\" width=\"200\"><b>" + tp.translate("Item Display Format") + "</b></td>");
        html.append("<td class=\"maintform_fieldtitle\" width=\"200\"><b>" + tp.translate("Documentation") + "</b></td>");
        html.append("</tr>");
        html.append("<tr id=\"" + primary.getValue(0, "tableid") + "_row\" onmouseover=\"style.background='LightGrey';style.cursor='Hand'\" onmouseout=\"style.background='White'\" onclick=\"selectTable( this.id )\">");
        if (isDevMode) {
            html.append("<td class=\"maintform_field\">&nbsp;</td>");
        }
        html.append("<td class=\"maintform_field\">0</td>");
        html.append("<td class=\"maintform_field\">").append(primary.getValue(0, "tableid")).append("</td>");
        html.append("<td class=\"maintform_field\">Primary</td>");
        html.append("<td class=\"maintform_field\">&nbsp;</td>");
        html.append("<td class=\"maintform_field\">").append(primary.getValue(0, "sdctype").equals("U") ? tp.translate("User") : tp.translate("System")).append("</td>");
        html.append("<td class=\"maintform_field\">&nbsp;</td>");
        if (primary.getValue(0, "sdctype").equals("U") || isDevMode || compCode.equals(sdcCompCode)) {
            html.append("<td class=\"maintform_field\"><input style=\"width:200px\" id=\"systable_" + primary.getValue(0, "tableid") + "_label\" name=\"systable_" + primary.getValue(0, "tableid") + "_label\" value=\"").append(primaryTable.getValue(0, "tablelabel")).append("\"></input></td>");
        } else {
            html.append("<td class=\"maintform_field\">").append(primaryTable.getValue(0, "tablelabel")).append("</td>");
        }
        if (primary.getValue(0, "sdctype").equals("U") || isDevMode || compCode.equals(sdcCompCode)) {
            html.append("<td class=\"maintform_field\"><input style=\"width:200px\" id=\"systable_" + primary.getValue(0, "tableid") + "_itemdisplay\" name=\"systable_" + primary.getValue(0, "tableid") + "_itemdisplay\" value=\"").append(primaryTable.getValue(0, "itemdisplay")).append("\"></input></td>");
        } else {
            html.append("<td class=\"maintform_field\">").append(primaryTable.getValue(0, "itemdisplay")).append("</td>");
        }
        if (primary.getValue(0, "sdctype").equals("U") || isDevMode || compCode.equals(sdcCompCode)) {
            html.append("<td class=\"maintform_field\"><textarea style=\"width:200px\" id=\"systable_" + primary.getValue(0, "tableid") + "\" name=\"systable_" + primary.getValue(0, "tableid") + "\">").append(primaryTable.getValue(0, "tabledoc")).append("</textarea></td>");
        } else {
            html.append("<td class=\"maintform_field\">").append(primaryTable.getValue(0, "tabledoc")).append("</td>");
        }
        html.append("</tr>");
        int maxSeq = 0;
        for (i = 0; i < detailTables.size(); ++i) {
            String tableid = detailTables.getValue(i, "linktableid");
            String linkid = detailTables.getValue(i, "linkid");
            String linktype = detailTables.getValue(i, "linktype");
            String tabletype = detailTables.getValue(i, "tabletype");
            maxSeq = Math.max(maxSeq, detailTables.getInt(i, "linksequence"));
            html.append("<tr id=\"" + tableid + "_row\" onmouseover=\"style.background='LightGrey';style.cursor='Hand'\" onmouseout=\"style.background='White'\" onclick=\"selectTable( this.id )\">");
            if (isDevMode) {
                html.append("<td class=\"maintform_field\"><input type=\"checkbox\" name=\"detailtable_selector\" id=\"detailtable_" + tableid + "\" linkid=\"" + linkid + "\"/></td>");
            }
            html.append("<td class=\"maintform_field\">").append(i + 1).append("</td>");
            html.append("<td class=\"maintform_field\">").append(tableid).append("</td>");
            html.append("<td class=\"maintform_field\">").append(linktype.equals("D") ? tp.translate("Detail") : tp.translate("Many-to-Many")).append("</td>");
            html.append("<td class=\"maintform_field\">").append(detailTables.getValue(i, "parenttableid")).append("</td>");
            html.append("<td class=\"maintform_field\">").append(tabletype.equals("C") || tabletype.equals("S") ? tp.translate("System") : tp.translate("User")).append("</td>");
            html.append("<td class=\"maintform_field\">").append(linkid).append("</td>");
            if (primary.getValue(0, "sdctype").equals("U") || isDevMode || compCode.equals(sdcCompCode)) {
                html.append("<td class=\"maintform_field\"><input style=\"width:200px\"  id=\"systable_" + tableid + "_label\" name=\"systable_" + tableid + "_label\"  value=\"").append(detailTables.getValue(i, "tablelabel")).append("\"></input></td>");
            } else {
                html.append("<td class=\"maintform_field\">").append(detailTables.getValue(0, "tablelabel")).append("</td>");
            }
            if (primary.getValue(0, "sdctype").equals("U") || isDevMode || compCode.equals(sdcCompCode)) {
                html.append("<td class=\"maintform_field\"><input style=\"width:200px\"  id=\"systable_" + tableid + "_itemdisplay\" name=\"systable_" + tableid + "_itemdisplay\"  value=\"").append(detailTables.getValue(i, "itemdisplay")).append("\"></input></td>");
            } else {
                html.append("<td class=\"maintform_field\">").append(detailTables.getValue(0, "itemdisplay")).append("</td>");
            }
            if (tabletype.equals("U") || isDevMode || compCode.equals(sdcCompCode)) {
                html.append("<td class=\"maintform_field\"><textarea style=\"width:200px\" id=\"systable_" + tableid + "\" name=\"systable_" + tableid + "\">").append(detailTables.getValue(i, "tabledoc")).append("</textarea></td>");
            } else {
                html.append("<td class=\"maintform_field\">").append(detailTables.getValue(i, "tabledoc")).append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</table>");
        html.append("<input type=\"hidden\" name=\"deletetablelist\" id=\"deletetablelist\" value=\"\"/>");
        if (this.element.getProperty("readonly", "N").equals("N")) {
            html.append("<table border = \"0\" cellspacing = \"0\" cellpadding = \"3\"><tr>");
            html.append("<td>");
            Button add = new Button(this.pageContext);
            add.setId("detailtable_add");
            add.setText(tp.translate("Add..."));
            add.setImg("WEB-CORE/images/gif/AddRow.gif");
            add.setAction("addTablePrompt()");
            html.append(add.getHtml());
            html.append("</td>");
            html.append("<td>");
            Button delete = new Button(this.pageContext);
            delete.setId("detailtable_delete");
            delete.setText(tp.translate("Remove..."));
            delete.setImg("WEB-CORE/images/gif/RemoveRow.gif");
            delete.setAction("removeTable()");
            html.append(delete.getHtml());
            html.append("</td>");
            html.append("</tr></table>");
        }
        html.append("<script>");
        html.append("var newlinksequence = " + (maxSeq + 1) + ";");
        html.append("var detailrowids = new Array();");
        html.append("var detaillinkids = new Array();");
        html.append("detailrowids[0] = '" + primary.getValue(0, "tableid") + "_row';");
        html.append("var detailtables = new Array();");
        for (i = 0; i < detailTables.size(); ++i) {
            html.append("detailtables[").append(i).append("] = '").append(detailTables.getValue(i, "linktableid")).append(" (").append(detailTables.getValue(i, "linkid")).append(")';");
            html.append("detailrowids[").append(i + 1).append("] = '").append(detailTables.getValue(i, "linktableid")).append("_row';");
            html.append("detaillinkids['").append(detailTables.getValue(i, "linktableid")).append("'] = '").append(detailTables.getValue(i, "linkid")).append("';");
        }
        html.append("</script>");
        return html.toString();
    }
}

