/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.advancedsearch;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.advancedsearch.SearchContent;
import com.labvantage.sapphire.pageelements.controls.Button;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DynamicGroupBy
extends SearchContent {
    String contentName = "xxx";

    @Override
    public String getHtml() {
        StringBuffer output = new StringBuffer();
        try {
            PropertyList listElementPropertyList = (PropertyList)this.pageContext.getAttribute("list", 2);
            if (listElementPropertyList != null) {
                PropertyListCollection plcGroupBy = listElementPropertyList.getCollection("groupby");
                Button okButton = new Button(this.pageContext);
                String groupByCallback = "";
                boolean showInitiallyGrouped = false;
                groupByCallback = this.element.getProperty("groupbycallback").equalsIgnoreCase("") ? "groupby" : this.element.getProperty("groupbycallback");
                showInitiallyGrouped = this.contentProperties.getProperty("showinitiallygrouped").equalsIgnoreCase("") || this.contentProperties.getProperty("showinitiallygrouped").equalsIgnoreCase("Y") || this.element.getProperty("showinitiallygrouped").equalsIgnoreCase("") || this.element.getProperty("showinitiallygrouped").equalsIgnoreCase("Y");
                okButton.setText("OK");
                okButton.setAction("doGroupBy()");
                okButton.setMargin("none");
                okButton.setAppearance("smalltext");
                output.append("\n<!-- START PAINTING THE GROUP BY SELECTOR -->\n");
                output.append("<table border=0 cellpadding=0 cellspacing=0 width=100%>\n");
                output.append("<form  id=frmGroupby name=frmGroupby style=\"padding:0px;margin-bottom:5px;margin-top:0px;margin-left:0px;margin-right:0px\">\n");
                output.append("        <tr>\n");
                output.append("        <td>\n");
                output.append("             <select id=groupbycolumns name=groupbycolumns class=\"search_inputfield\" >\n");
                output.append("                        <option value=\"NONE\" " + (!showInitiallyGrouped ? "selected>" : ">") + " None\n");
                for (int index = 0; index < plcGroupBy.size(); ++index) {
                    PropertyList plGroupBy = plcGroupBy.getPropertyList(index);
                    output.append("                    <option value=\"" + plGroupBy.getProperty("columnid") + "\"" + (index == 0 && showInitiallyGrouped ? "selected>" : ">") + plGroupBy.getProperty("title") + "</option>");
                }
                output.append("             </select>\n");
                output.append("        </td>\n");
                output.append("        <td align=left>\n");
                output.append("        " + okButton.getHtml() + "\n");
                output.append("        </td>\n");
                output.append("    </tr>\n");
                output.append("</form>\n");
                output.append("</table>\n\n");
                output.append("<script>\n");
                output.append("    function doGroupBy(){\n");
                output.append("        selectedindex=document.frmGroupby.groupbycolumns.selectedIndex;\n");
                output.append("        selectedcolumn=document.frmGroupby.groupbycolumns.options[selectedindex].value;\n");
                output.append("        try{\n");
                output.append("             " + groupByCallback + "(selectedindex, selectedcolumn);\n");
                output.append("        }\n");
                output.append("        catch(err){\n");
                output.append("            sapphire.alert(\"" + this.translator.translate("Callback javascript not found in the jsp. Cannot group the list.") + "\");\n");
                output.append("            return false;\n");
                output.append("        }\n");
                output.append("    }\n");
                output.append("</script>\n\n");
                output.append("<!-- END PAINTING THE GROUP BY SELECTOR -->\n\n");
            }
        }
        catch (Exception ex) {
            Trace.log("Error", "Exception at DynamicGroupBy.getHTml()->" + ex);
            return "";
        }
        return output.toString();
    }
}

