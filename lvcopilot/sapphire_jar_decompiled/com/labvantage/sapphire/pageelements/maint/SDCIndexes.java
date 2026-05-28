/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.services.DDTConstants;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;

public class SDCIndexes
extends BaseElement
implements DDTConstants {
    @Override
    public String getHtml() {
        this.logger.info("Creating index element");
        StringBuffer html = new StringBuffer();
        QueryProcessor qp = new QueryProcessor(this.pageContext);
        boolean isDevMode = false;
        try {
            isDevMode = "Y".equals(new ConfigurationProcessor(this.pageContext).getSysConfigProperty("devmode"));
        }
        catch (SapphireException sapphireException) {
            // empty catch block
        }
        DataSet primary = this.sdiInfo.getSDIData().getDataset("primary");
        String sdcid = primary.getValue(0, "sdcid");
        DataSet indexes = qp.getPreparedSqlDataSet("SELECT tableid, reftypeflag, sysref.refid, columnid, columnsequence, storageid, refindexid FROM   sysrefcolumn,sysref WHERE  sysrefcolumn.refid = sysref.refid AND sysref.reftypeflag <> 'F' AND sysref.tableid IN (       SELECT tableid FROM sdc WHERE sdcid = ?        UNION        SELECT linktableid FROM sdc, sdclink, systable WHERE sdc.sdcid = sdclink.sdcid AND sdclink.linktableid = systable.tableid AND sdc.sdcid = ? AND linktype IN ( 'D', 'M' )        UNION        SELECT linktableid FROM sdcdetaillink, systable WHERE sdcdetaillink.linktableid = systable.tableid AND sdcid = ? AND linktype IN ( 'D', 'M' ) ) ORDER BY tableid, sysref.refid DESC, columnsequence", new Object[]{sdcid, sdcid, sdcid});
        html.append("<table id=\"indexes_table\" class=\"gridmaint_table\" border = \"1\" cellspacing = \"0\" cellpadding = \"3\" width=\"100%\">");
        html.append("<tr>");
        html.append("<td class=\"gridmaint_fieldtitle\">&nbsp;</td>");
        html.append("<td class=\"gridmaint_fieldtitle\" width=\"150\"><b>Type</b></td>");
        html.append("<td class=\"gridmaint_fieldtitle\" width=\"150\"><b>Name</b></td>");
        html.append("<td class=\"gridmaint_fieldtitle\" width=\"250\"><b>Columns</b></td>");
        html.append("</tr>");
        int i = 0;
        while (i < indexes.size()) {
            String tableid = indexes.getValue(i, "tableid");
            String refid = indexes.getValue(i, "refid");
            String reftypeflag = indexes.getValue(i, "reftypeflag");
            html.append("<tr id=\"index_row" + i + "\" tableid=\"" + tableid + "\">");
            html.append("<td class=\"gridmaint_field\">" + (reftypeflag.equals("P") ? "&nbsp;" : "<input type=\"checkbox\" name=\"index_selector\" id=\"index_" + refid + "\" tableid=\"" + tableid + "\"/>") + "</td>");
            html.append("<td class=\"gridmaint_field\" valign=\"top\">").append(reftypeflag.equals("P") ? "Primary Key Index" : (reftypeflag.equals("U") ? "Unique Index" : (reftypeflag.equals("I") ? "Non-unique Index" : "Foreign Key"))).append("</td>");
            html.append("<td class=\"gridmaint_field\" valign=\"top\">").append(refid).append("</td>");
            html.append("<td class=\"gridmaint_field\">");
            while (i < indexes.size() && refid.equals(indexes.getValue(i, "refid"))) {
                html.append(indexes.getValue(i, "columnid")).append("<br/>");
                ++i;
            }
            html.append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");
        html.append("<input type=\"hidden\" name=\"deleteindexlist\" id=\"deleteindexlist\" value=\"\"/>");
        html.append("<input type=\"hidden\" name=\"addindextableid\" id=\"addindextableid\" value=\"\"/>");
        html.append("<input type=\"hidden\" name=\"addindexcolumnids\" id=\"addindexcolumnids\" value=\"\"/>");
        html.append("<input type=\"hidden\" name=\"addindextype\" id=\"addindextype\" value=\"\"/>");
        if (this.element.getProperty("readonly", "N").equals("N")) {
            html.append("<table border = \"0\" cellspacing = \"0\" cellpadding = \"3\"><tr>");
            html.append("<td>");
            Button add = new Button(this.pageContext);
            add.setId("index_add");
            add.setText("Add...");
            add.setImg("WEB-CORE/images/gif/AddRow.gif");
            add.setAction("addIndexPrompt()");
            html.append(add.getHtml());
            html.append("</td>");
            html.append("<td>");
            Button delete = new Button(this.pageContext);
            delete.setId("index_delete");
            delete.setText("Remove...");
            delete.setImg("WEB-CORE/images/gif/RemoveRow.gif");
            delete.setAction("removeIndex()");
            html.append(delete.getHtml());
            html.append("</td>");
            html.append("</tr></table>");
        }
        html.append("<script>");
        html.append("var indexrows = ").append(indexes.size()).append(";");
        html.append("</script>");
        this.logger.info("Created index element");
        return html.toString();
    }
}

