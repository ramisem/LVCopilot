/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.elements.advancedsearch;

import com.labvantage.sapphire.pageelements.controls.Button;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DynamicGroupBy
extends BaseElement {
    private String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private TranslationProcessor __Tp;

    public DynamicGroupBy(PageContext pageContext, String connectionid) {
        this.pageContext = pageContext;
        this.setConnectionId(connectionid);
        this.setTranslationProcessor();
    }

    private void setTranslationProcessor() {
        this.__Tp = new TranslationProcessor(this.pageContext);
    }

    @Override
    public String getHtml() {
        StringBuffer sbHtml = new StringBuffer("");
        PropertyList plList = null;
        try {
            plList = (PropertyList)this.pageContext.getAttribute("list", 2);
            if (plList != null) {
                PropertyListCollection plcGroupBy = plList.getCollection("groupby");
                PropertyList plGroupBy = null;
                Button btnOk = new Button(this.pageContext);
                String groupByCallback = "";
                boolean showInitiallyGrouped = false;
                groupByCallback = this.element.getProperty("groupbycallback").equalsIgnoreCase("") ? "groupby" : this.element.getProperty("groupbycallback");
                showInitiallyGrouped = this.element.getProperty("showinitiallygrouped").equalsIgnoreCase("") || this.element.getProperty("showinitiallygrouped").equalsIgnoreCase("Y");
                btnOk.setAppearance("scansearch");
                btnOk.setText(this.__Tp.translate("OK"));
                btnOk.setAction("doGroupBy()");
                btnOk.setMargin("none");
                btnOk.setStyle("width:25px; margin-left:3px; height:18px; ");
                sbHtml.append("\n<!-- START PAINTING THE GROUP BY SELECTOR -->\n");
                sbHtml.append("<form  id=frmGroupby name=frmGroupby style=\"padding:0px;margin-bottom:5px;margin-top:0px;margin-left:0px;margin-right:0px\">\n");
                sbHtml.append("<table class=groupby_table border=0 cellpadding=0 cellspacing=0 width=10%>\n");
                sbHtml.append("        <tr class=search_header>\n");
                sbHtml.append("        <td colspan=2 nowrap><b>Group by Columns:</b></td>\n");
                sbHtml.append("    </tr>\n");
                sbHtml.append("        <tr>\n");
                sbHtml.append("        <td>\n");
                sbHtml.append("             <select id=groupbycolumns name=groupbycolumns class=search_groupby_select>\n");
                sbHtml.append("                        <option value=\"NONE\" " + (!showInitiallyGrouped ? "selected>" : ">") + " None\n");
                for (int index = 0; index < plcGroupBy.size(); ++index) {
                    plGroupBy = plcGroupBy.getPropertyList(index);
                    sbHtml.append("                    <option value=\"" + plGroupBy.getProperty("columnid") + "\"" + (index == 0 && showInitiallyGrouped ? "selected>" : ">") + plGroupBy.getProperty("title") + "</option>");
                }
                sbHtml.append("             </select>\n");
                sbHtml.append("        </td>\n");
                sbHtml.append("        <td align=left>\n");
                sbHtml.append("        " + btnOk.getHtml() + "\n");
                sbHtml.append("        </td>\n");
                sbHtml.append("    </tr>\n");
                sbHtml.append("</table>\n\n");
                sbHtml.append("</form>\n");
                sbHtml.append("<script>\n");
                sbHtml.append("    function doGroupBy(){\n");
                sbHtml.append("        selectedindex=document.frmGroupby.groupbycolumns.selectedIndex;\n");
                sbHtml.append("        selectedcolumn=document.frmGroupby.groupbycolumns.options[selectedindex].value;\n");
                sbHtml.append("        try{\n");
                sbHtml.append("             " + groupByCallback + "(selectedindex, selectedcolumn);\n");
                sbHtml.append("        }\n");
                sbHtml.append("        catch(err){\n");
                sbHtml.append("            sapphire.alert(\"" + this.__Tp.translate("Callback javascript not found in the jsp. Cannot group the list.") + "\");\n");
                sbHtml.append("            return false;\n");
                sbHtml.append("        }\n");
                sbHtml.append("    }\n");
                sbHtml.append("</script>\n\n");
                sbHtml.append("<!-- END PAINTING THE GROUP BY SELECTOR -->\n\n");
                plcGroupBy = null;
                plGroupBy = null;
                btnOk = null;
            }
        }
        catch (Exception ex) {
            this.logger.error("Exception at DynamicGroupBy.getHTml()->" + ex, ex);
            return "";
        }
        plList = null;
        return sbHtml.toString();
    }
}

