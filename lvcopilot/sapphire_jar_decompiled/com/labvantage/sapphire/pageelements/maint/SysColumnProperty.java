/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.services.DDTConstants;
import java.util.HashMap;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SysColumnProperty
extends BaseElement
implements DDTConstants {
    @Override
    public String getHtml() {
        TranslationProcessor tp = this.getTranslationProcessor();
        this.logger.info("Creating syscolumnproperty element");
        StringBuffer html = new StringBuffer();
        QueryProcessor qp = new QueryProcessor(this.pageContext);
        DataSet primary = this.sdiInfo.getSDIData().getDataset("primary");
        String sdcid = primary.getValue(0, "sdcid");
        SDCProcessor sp = new SDCProcessor(this.pageContext);
        DataSet syscolumns = this.sdiInfo.getDataSet("syscolumn");
        SafeSQL safeSQL = new SafeSQL();
        DataSet syscolumnproperties = qp.getPreparedSqlDataSet("SELECT * FROM syscolumnproperty WHERE tableid IN (   SELECT distinct syscolumn.tableid    FROM syscolumn, sdclink    WHERE syscolumn.tableid = sdclink.linktableid AND          sdclink.linktype IN ( 'D', 'M' ) AND          sdclink.sdcid = " + safeSQL.addVar(sdcid) + "    UNION    SELECT tableid    FROM sdc    WHERE sdcid =" + safeSQL.addVar(sdcid) + ")", safeSQL.getValues());
        html.append("<table class=\"gridmaint_table\" border=\"1\" cellspacing=\"0\" width=\"100%\">");
        html.append("<tr class=\"gridmaint_tablehead\" height=\"8\">");
        html.append("<td class=\"gridmaint_fieldtitle\"><input type=\"checkbox\"/></td>");
        html.append("<td class=\"gridmaint_fieldtitle\"><b>" + tp.translate("Column Name") + "</b></td>");
        html.append("<td class=\"gridmaint_fieldtitle\"><b>&nbsp;" + tp.translate("Key Generation") + "&nbsp;</b></td>");
        html.append("<td class=\"gridmaint_fieldtitle\"><b>&nbsp;" + tp.translate("Time Zone Independent") + "&nbsp;</b></td>");
        html.append("<td class=\"gridmaint_fieldtitle\"><b>&nbsp;" + tp.translate("Searchable") + "&nbsp;</b></td>");
        html.append("<td class=\"gridmaint_fieldtitle\"><b>&nbsp;" + tp.translate("Default Editor Style") + "&nbsp;</b></td>");
        html.append("</tr>");
        PropertyListCollection links = sp.getColumns(sdcid);
        HashMap<String, String> findMap = new HashMap<String, String>();
        for (int i = 0; i < syscolumns.size(); ++i) {
            int findRow;
            String tableid = syscolumns.getValue(i, "tableid");
            String columnid = syscolumns.getValue(i, "columnid");
            String searchable = syscolumns.getValue(i, "searchableflag");
            findMap.put("columnid", columnid);
            html.append("<tr id=\"syscolumnproperty_row" + i + "\" tableid=\"" + tableid + "\">");
            html.append("<td class=\"gridmaint_field\">&nbsp;</td>");
            html.append("<td class=\"gridmaint_field\"><input type=\"text\" disabled class=\"mandatoryfield\" value=\"").append(columnid).append("\"/></td>");
            if ("Y".equals(syscolumns.getValue(i, "pkflag"))) {
                boolean m2m = false;
                for (int j = 0; !m2m && j < links.size(); ++j) {
                    PropertyList link = links.getPropertyList(j);
                    m2m = link.getProperty("linktableid").equals(tableid) && link.getProperty("linktype").equals("M");
                }
                if (m2m || sp.getProperty(sdcid, "sdctype").equals("C") || sp.getProperty(sdcid, "sdctype").equals("D")) {
                    html.append("<td>" + tp.translate("Primary key") + "</td>");
                } else if (columnid.equals(sp.getProperty(sdcid, "keycolid1")) || columnid.equals(sp.getProperty(sdcid, "keycolid2")) || columnid.equals(sp.getProperty(sdcid, "keycolid3"))) {
                    if (tableid.equals(sp.getProperty(sdcid, "tableid")) && columnid.equals(sp.getProperty(sdcid, "keycolid1"))) {
                        String id = "primary_keygenerationrule";
                        String value = primary.getValue(0, "keygenerationrule");
                        html.append(this.getKeyHtml(id, value, columnid));
                    } else {
                        html.append("<td>" + tp.translate("Primary key") + "</td>");
                    }
                } else {
                    findMap.put("propertyid", "keygenerationrule");
                    int findRow2 = syscolumnproperties.findRow(findMap);
                    String id = "syscolumnproperty__" + tableid + "__" + columnid + "__" + "keygenerationrule";
                    String value = findRow2 >= 0 ? syscolumnproperties.getValue(findRow2, "propertyvalue") : "";
                    html.append(this.getKeyHtml(id, value, columnid));
                }
            } else {
                html.append("<td>&nbsp;</td>");
            }
            if (syscolumns.getValue(i, "datatype").equals("D")) {
                findMap.put("propertyid", "timezoneindependent");
                findRow = syscolumnproperties.findRow(findMap);
                String value = findRow >= 0 ? syscolumnproperties.getValue(findRow, "propertyvalue") : "";
                html.append("<td class=\"gridmaint_field\"><select name=\"syscolumnproperty__" + tableid + "__" + columnid + "__" + "timezoneindependent" + "\" onchange=\"setChangesMade(true)\">");
                html.append("<option value=\"Y\"" + (value.equals("Y") ? "SELECTED" : "") + ">" + tp.translate("Yes") + "</option>");
                html.append("<option value=\"N\"" + (value.length() == 0 || value.equals("N") ? "SELECTED" : "") + ">" + tp.translate("No") + "</option>");
                html.append("</select>");
            } else {
                html.append("<td>&nbsp;</td>");
            }
            html.append("<td class=\"gridmaint_field\"><input type=\"checkbox\" ").append(searchable.equals("Y") ? "checked" : "").append(" syscolumnrow=\"").append(i).append("\" onclick=\"setSearchable( this )\"/></td>");
            findMap.put("propertyid", "editorstyleid");
            findRow = syscolumnproperties.findRow(findMap);
            String value = findRow >= 0 ? syscolumnproperties.getValue(findRow, "propertyvalue") : "";
            String editorstyleid = "syscolumnproperty__" + tableid + "__" + columnid + "__" + "editorstyleid";
            html.append("<td class=\"gridmaint_field\"><input type=\"input\"").append(" name=\"" + editorstyleid + "\"").append(" id=\"" + editorstyleid + "\"").append(" value=\"" + value + "\"").append(" readonly").append(" onkeydown=\"if(event.keyCode==46) {this.value='';}\"").append(" onchange=\"setChangesMade(true)\"").append(" syscolumnrow=\"").append(i).append("\"/>").append(" <script>var oLUPD_sdcattributedef_editorstyleid={\"selectortype\":\"\",\"layout\":{\"hidetitle\":\"Y\"},\"sdcid\":\"LV_EditorStyle\"};</script>").append("<a style=\"display:inline;\" displayStyle=\"inline\" id=\"" + editorstyleid + "_img\" href=\"#\" onClick=\"lookupfield('" + editorstyleid + "','LV_EditorStyle','','N','','','','','','','editorstyleid','rc?command=page&page=LV_EditorStyleLookup','',true,oLUPD_sdcattributedef_editorstyleid);return false;\" tabindex=\"0\"><img title=\"" + tp.translate("Lookup Editor Style") + "\" border=\"0\" src=\"WEB-CORE/imageref/flat/16/flat_black_external_lookup1.svg\" class=\"lookup_img\"></a></td>");
            html.append("</tr>");
        }
        html.append("</table>");
        html.append("<script>");
        html.append("var syscolumnpropertyrows = " + syscolumns.size() + ";");
        html.append("</script>");
        this.logger.info("Created syscolumnproperty element");
        return html.toString();
    }

    private String getKeyHtml(String id, String value, String columnid) {
        StringBuffer html = new StringBuffer();
        TranslationProcessor tp = this.getTranslationProcessor();
        html.append("<td class=\"gridmaint_field\"><input type=\"hidden\" value=\"" + value + "\" name=\"" + id + "\" id=\"" + id + "\" columnid=\"" + columnid + "\">");
        html.append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr><td id=\"" + id + "_status\" width=\"100\">");
        html.append(value.length() > 0 && value.startsWith("A") ? tp.translate("Defined - active") : (value.length() > 0 && value.startsWith("N") ? tp.translate("Defined - inactive") : tp.translate("Not defined")));
        html.append("</td><td align=\"right\">");
        html.append("<button type=\"button\" style=\"height:21px\" onclick=\"editKeygenRulePrompt( '" + id + "' )\">...</button></td></tr></table></td>");
        return html.toString();
    }
}

