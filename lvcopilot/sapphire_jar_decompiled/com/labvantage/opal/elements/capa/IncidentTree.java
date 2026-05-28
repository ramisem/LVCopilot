/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.capa;

import com.labvantage.opal.util.IncidentUtil;
import java.util.HashMap;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class IncidentTree
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 70072 $";
    PropertyList pagedata = null;
    QueryProcessor qp = null;
    TranslationProcessor tp = null;
    String incidentId = "";
    DataSet dsIncident = new DataSet();
    DataSet dsIncident_Finding = new DataSet();
    DataSet dsIncident_ActionPlan = new DataSet();
    DataSet dsIncident_Workorder = new DataSet();
    DataSet dsFinding_ActionPlan = new DataSet();
    DataSet dsFinding_Workorder = new DataSet();
    DataSet dsIncident_ActionPlan_Workorder = new DataSet();
    DataSet dsFinding_ActionPlan_Workorder = new DataSet();
    boolean initiallyCollapsed = true;
    DataSet dsDisplayColumns = new DataSet();

    @Override
    public String getHtml() {
        StringBuffer sbHtml = new StringBuffer("");
        try {
            this.pagedata = (PropertyList)this.pageContext.getRequest().getAttribute("pagedata");
            this.qp = this.getQueryProcessor();
            this.tp = this.getTranslationProcessor();
            this.incidentId = this.pagedata.getProperty("keyid1");
            this.dsDisplayColumns.addColumnValues("type", 0, "incident;finding;actionplan;workorder", ";");
            this.dsDisplayColumns.addColumnValues("idcolumn", 0, "incidentid;incidentfindid;actionplanid;workorderid", ";");
            this.dsDisplayColumns.addColumn("columns", -1);
            if (this.element.getPropertyList("columns") != null) {
                this.dsDisplayColumns.setObject(this.dsDisplayColumns.findRow("type", "incident"), "columns", this.element.getPropertyList("columns").getCollection("incidentcolumns"));
                this.dsDisplayColumns.setObject(this.dsDisplayColumns.findRow("type", "finding"), "columns", this.element.getPropertyList("columns").getCollection("findingcolumns"));
                this.dsDisplayColumns.setObject(this.dsDisplayColumns.findRow("type", "actionplan"), "columns", this.element.getPropertyList("columns").getCollection("plancolumns"));
                this.dsDisplayColumns.setObject(this.dsDisplayColumns.findRow("type", "workorder"), "columns", this.element.getPropertyList("columns").getCollection("workordercolumns"));
            }
            this.initiallyCollapsed = this.element.getProperty("initiallycollapsed").equalsIgnoreCase("Y");
            if (!this.incidentId.equalsIgnoreCase("")) {
                this.incidentId = StringUtil.replaceAll(this.incidentId, ";", "','");
                this.createDatasets();
                sbHtml.append(this.getJavascript());
                sbHtml.append("<table cellpadding=5 cellspacing=0 border=0>\n    <tr>\n        <td><div class=findingheader style=\"height:15px; width:15px\"></div></td><td>" + this.tp.translate("Incident Findings") + "</td>\n        <td><div class=actionplanheader style=\"height:15px; width:15px\"></div></td><td>" + this.tp.translate("Action Plans") + "</td>\n        <td><div class=workorderheader style=\"height:15px; width:15px\"></div></td><td>" + this.tp.translate("Workorders") + "</td>\n    </tr>\n</table>\n<br>\n");
                sbHtml.append(this.getIncidentTable());
            } else {
                sbHtml.append(this.tp.translate("No keyid1 found in the request. Need to pass in a valid incidentid to view details.") + "\n");
            }
        }
        catch (Exception ex) {
            return ex.getMessage();
        }
        return sbHtml.toString();
    }

    private StringBuffer getJavascript() {
        StringBuffer sbHtml = new StringBuffer("");
        sbHtml.append("<script>\n\n    function collapsyExpandy( elId ) {\n        var imgObj = document.getElementById(\"img_\" + elId);\n        var contentObj = document.getElementById(\"content_\" + elId);\n        //alert(imgObj.src + \"\\nstate=\" + imgObj.currstate);\n        if ( imgObj.getAttribute(\"currstate\") == \"collapsed\" ) {\n            imgObj.setAttribute(\"currstate\",\"expanded\");\n            imgObj.src = \"WEB-OPAL/images/minus.gif\";\n            contentObj.classList.remove(\"collapsedcontent\");\n        }\n        else {\n            imgObj.setAttribute(\"currstate\",\"collapsed\");\n            imgObj.src = \"WEB-OPAL/images/plus.gif\";\n            contentObj.className = \"collapsedcontent\";\n        }\n    }\n\n</script>\n");
        return sbHtml;
    }

    private void createDatasets() {
        HashMap hmDatasets = IncidentUtil.getIncidentDetailDatasets(this.incidentId, this.qp);
        this.dsIncident = (DataSet)hmDatasets.get("Incident");
        this.dsIncident_Finding = (DataSet)hmDatasets.get("Incident_Finding");
        this.dsIncident_ActionPlan = (DataSet)hmDatasets.get("Incident_ActionPlan");
        this.dsIncident_Workorder = (DataSet)hmDatasets.get("Incident_Workorder");
        this.dsFinding_ActionPlan = (DataSet)hmDatasets.get("Finding_ActionPlan");
        this.dsFinding_Workorder = (DataSet)hmDatasets.get("Finding_Workorder");
        this.dsIncident_ActionPlan_Workorder = (DataSet)hmDatasets.get("Incident_ActionPlan_Workorder");
        this.dsFinding_ActionPlan_Workorder = (DataSet)hmDatasets.get("Finding_ActionPlan_Workorder");
    }

    private StringBuffer getColumnHeaders(String type, boolean addFirstBlankTd) {
        StringBuffer sbHtml = new StringBuffer("");
        int row = this.dsDisplayColumns.findRow("type", type);
        if (row >= 0) {
            PropertyListCollection columns = (PropertyListCollection)this.dsDisplayColumns.getObject(row, "columns");
            String cssClass = type + "header";
            if (addFirstBlankTd) {
                sbHtml.append("    <td><img src=\"WEB-OPAL/images/spacer.gif\" class=\"spacer\"/></td>\n");
            }
            for (int i = 0; i < columns.size(); ++i) {
                String title = columns.getPropertyList(i).getProperty("title");
                if (title.length() == 0) {
                    title = StringUtil.initCaps(columns.getPropertyList(i).getProperty("columnid"));
                }
                sbHtml.append("    <td col=" + (i + 1) + " class=" + cssClass + ">" + title + "</td>\n");
            }
        }
        return sbHtml;
    }

    private StringBuffer getColumns(String type, DataSet ds, int row) {
        StringBuffer sbHtml = new StringBuffer("");
        int typeRow = this.dsDisplayColumns.findRow("type", type);
        if (typeRow >= 0) {
            PropertyListCollection columns = (PropertyListCollection)this.dsDisplayColumns.getObject(typeRow, "columns");
            String idColumn = this.dsDisplayColumns.getValue(typeRow, "idcolumn");
            String cssClass = type + "cell";
            for (int i = 0; i < columns.size(); ++i) {
                String columnId = columns.getPropertyList(i).getProperty("columnid");
                String value = ds.getValue(row, columnId, "&nbsp;");
                String link = columns.getPropertyList(i).getProperty("link");
                String identifier = ds.getValue(row, idColumn, "&nbsp;");
                sbHtml.append("    <td col=" + (i + 1) + " class=" + cssClass + ">\n");
                if (columnId.equals("incidentid") && link.length() > 0 && link.indexOf(";") >= 0) {
                    int incRow = this.dsIncident.findRow("incidentid", value);
                    String incidentCategory = "";
                    if (incRow >= 0) {
                        incidentCategory = this.dsIncident.getValue(incRow, "incidentcategory", "");
                    }
                    try {
                        String[] tempLink = StringUtil.split(link, ";");
                        link = incidentCategory.equals("Planned") ? tempLink[0] : (incidentCategory.equals("UnPlanned") ? tempLink[1] : tempLink[2]);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (link.length() > 0) {
                    link = link.indexOf("[identifier]") >= 0 ? StringUtil.replaceAll(link, "[identifier]", identifier, false) : (link.indexOf("[value]") >= 0 ? StringUtil.replaceAll(link, "[value]", value, false) : link + "&keyid1=" + HttpUtil.encodeURIComponent(identifier));
                    if (this.browser.isIE()) {
                        sbHtml.append("<a href=\"" + link + "\" target=new>" + this.tp.translate(value) + "</a>\n");
                    } else {
                        sbHtml.append("<a href=\"#\" onclick=Javascript:sapphire.lookup.open('" + link + "')>" + this.tp.translate(value) + "</a>\n");
                    }
                } else {
                    sbHtml.append(this.tp.translate(value) + "\n");
                }
                sbHtml.append("     </td>\n");
            }
        }
        return sbHtml;
    }

    private StringBuffer getIncidentTable() {
        StringBuffer sbHtml = new StringBuffer("");
        sbHtml.append("<!-- START INCIDENT TABLE -->\n");
        sbHtml.append("<table border=0 cellpadding=\"3\" cellspacing=\"0\" summary=\"incident table\">\n");
        boolean hasAnyChildren = false;
        hasAnyChildren = this.dsIncident_Finding.size() > 0 || this.dsIncident_ActionPlan.size() > 0 || this.dsIncident_Workorder.size() > 0;
        for (int i = 0; i < this.dsIncident.size(); ++i) {
            sbHtml.append("    <tr num_incidents=" + this.dsIncident.size() + " >\n");
            sbHtml.append(this.getColumnHeaders("incident", hasAnyChildren));
            sbHtml.append("    </tr>\n");
            String currIncidentId = this.dsIncident.getValue(i, "incidentid");
            sbHtml.append("    <tr incident_index=" + (i + 1) + " incidentid=\"" + currIncidentId + "\">\n");
            HashMap<String, String> hmFilter = new HashMap<String, String>();
            hmFilter.clear();
            hmFilter.put("incidentid", currIncidentId);
            DataSet dsFindingsForThisIncident = this.dsIncident_Finding.getFilteredDataSet(hmFilter);
            DataSet dsPlansForThisIncident = this.dsIncident_ActionPlan.getFilteredDataSet(hmFilter);
            hmFilter.clear();
            hmFilter.put("sourcesdcid", "LV_Incdt");
            hmFilter.put("sourcekeyid1", currIncidentId);
            DataSet dsWorkordersForThisIncident = this.dsIncident_Workorder.getFilteredDataSet(hmFilter);
            boolean hasChildren = false;
            hasChildren = dsFindingsForThisIncident.size() > 0 || dsPlansForThisIncident.size() > 0 || dsWorkordersForThisIncident.size() > 0;
            String tooltipText = "";
            if (dsFindingsForThisIncident.size() > 0) {
                tooltipText = tooltipText + dsFindingsForThisIncident.size() + " Findings ";
            }
            if (dsPlansForThisIncident.size() > 0) {
                tooltipText = tooltipText + dsPlansForThisIncident.size() + " ActionPlans ";
            }
            if (dsWorkordersForThisIncident.size() > 0) {
                tooltipText = tooltipText + dsWorkordersForThisIncident.size() + " Workorders ";
            }
            if (hasChildren) {
                sbHtml.append("        <td valign=\"middle\"><img ");
                if (this.initiallyCollapsed) {
                    sbHtml.append("src=\"WEB-OPAL/images/plus.gif\" currstate=\"collapsed\" ");
                } else {
                    sbHtml.append("src=\"WEB-OPAL/images/minus.gif\" currstate=\"expanded\" ");
                }
                sbHtml.append("id =\"img_" + currIncidentId + "\" class=\"collapsyexpandy\" onclick=\"collapsyExpandy('" + currIncidentId + "')\" title=\"" + tooltipText + "\"/></td>\n");
            } else if (hasAnyChildren) {
                sbHtml.append("        <td>&nbsp;</td>\n");
            }
            sbHtml.append(this.getColumns("incident", this.dsIncident, i));
            sbHtml.append("    </tr>\n");
            if (!hasChildren) continue;
            if (this.browser.isIE()) {
                sbHtml.append("    <tr class=\"" + (this.initiallyCollapsed ? "collapsedcontent" : "expandedcontent") + "\" id=\"content_" + currIncidentId + "\">\n");
            } else {
                sbHtml.append("    <tr>\n");
            }
            sbHtml.append("        <td><img src=\"WEB-OPAL/images/spacer.gif\" class=\"spacer\"/></td>\n");
            int numColumns = ((PropertyListCollection)this.dsDisplayColumns.getObject(this.dsDisplayColumns.findRow("type", "incident"), "columns")).size();
            sbHtml.append("        <td colspan=\"" + numColumns + "\">\n");
            sbHtml.append("            <!-- START INCIDENT CHILDREN WRAPPER TABLE -->\n");
            if (this.browser.isIE()) {
                sbHtml.append("            <table border=0 cellpadding=\"3\" cellspacing=\"0\" summary=\"incident children wrapper table\">\n");
            } else {
                sbHtml.append("            <table class=\"" + (this.initiallyCollapsed ? "collapsedcontent" : "expandedcontent") + "\" id=\"content_" + currIncidentId + "\" border=0 cellpadding=\"3\" cellspacing=\"0\" summary=\"incident children wrapper table\">\n");
            }
            if (dsFindingsForThisIncident.size() > 0) {
                sbHtml.append("            <tr><td>\n");
                sbHtml.append("            " + this.getFindingTable(currIncidentId, dsFindingsForThisIncident));
                sbHtml.append("            </td></tr>\n");
            }
            if (dsPlansForThisIncident.size() > 0) {
                sbHtml.append("            <tr><td>\n");
                sbHtml.append("            " + this.getPlanTable("incident", currIncidentId, dsPlansForThisIncident));
                sbHtml.append("            </td></tr>\n");
            }
            if (dsWorkordersForThisIncident.size() > 0) {
                sbHtml.append("            <tr><td>\n");
                sbHtml.append(this.getWorkorderTable("incident", currIncidentId, dsWorkordersForThisIncident));
                sbHtml.append("            </td></tr>\n");
            }
            sbHtml.append("        </table>\n");
            sbHtml.append("        <!-- END INCIDENT CHILDREN WRAPPER TABLE -->\n");
            sbHtml.append("    </td>\n");
            sbHtml.append("</tr>\n");
        }
        if (this.dsIncident.size() == 0) {
            sbHtml.append("<tr>\n");
            sbHtml.append(" <td colspan=" + ((PropertyListCollection)this.dsDisplayColumns.getObject(this.dsDisplayColumns.findRow("type", "incident"), "columns")).size() + " >\n");
            sbHtml.append(this.tp.translate("No rows found for incidentid") + " = " + this.incidentId + "\n");
            sbHtml.append(" </td>\n");
            sbHtml.append("</tr>\n");
        }
        sbHtml.append("</table>\n");
        sbHtml.append("<!-- END INCIDENT TABLE -->\n");
        return sbHtml;
    }

    private StringBuffer getFindingTable(String incidentId, DataSet dsFindingsForAnIncident) {
        StringBuffer sbHtml = new StringBuffer("");
        sbHtml.append("<!-- START FINDING TABLE -->\n");
        sbHtml.append("<table id=\"content_incident_findings\" parentid=\"" + incidentId + "\" border=0 cellpadding=\"3\" cellspacing=\"0\" summary=\"finding content table\">\n");
        boolean hasChildren = false;
        hasChildren = this.dsFinding_ActionPlan.size() > 0 || this.dsFinding_Workorder.size() > 0;
        for (int i = 0; i < dsFindingsForAnIncident.size(); ++i) {
            sbHtml.append("    <tr num_findings=" + dsFindingsForAnIncident.size() + " >\n");
            sbHtml.append(this.getColumnHeaders("finding", true));
            sbHtml.append("    </tr>\n");
            String currFindingId = dsFindingsForAnIncident.getValue(i, "incidentfindid");
            sbHtml.append("    <tr finding_index=" + (i + 1) + " findingid=\"" + currFindingId + "\" >\n");
            HashMap<String, String> hmFilter = new HashMap<String, String>();
            hmFilter.put("incidentfindid", currFindingId);
            DataSet dsPlansForThisFinding = this.dsFinding_ActionPlan.getFilteredDataSet(hmFilter);
            hmFilter.clear();
            hmFilter.put("sourcesdcid", "LV_IncdtFind");
            hmFilter.put("sourcekeyid1", currFindingId);
            DataSet dsWorkordersForThisFinding = this.dsFinding_Workorder.getFilteredDataSet(hmFilter);
            hasChildren = dsPlansForThisFinding.size() > 0 || dsWorkordersForThisFinding.size() > 0;
            String tooltipText = "";
            if (dsPlansForThisFinding.size() > 0) {
                tooltipText = tooltipText + dsPlansForThisFinding.size() + " ActionPlans ";
            }
            if (dsWorkordersForThisFinding.size() > 0) {
                tooltipText = tooltipText + dsWorkordersForThisFinding.size() + " Workorders ";
            }
            if (hasChildren) {
                sbHtml.append("        <td valign=\"middle\"><img ");
                if (this.initiallyCollapsed) {
                    sbHtml.append("src=\"WEB-OPAL/images/plus.gif\" currstate=\"collapsed\" ");
                } else {
                    sbHtml.append("src=\"WEB-OPAL/images/minus.gif\" currstate=\"expanded\" ");
                }
                sbHtml.append("id=\"img_" + currFindingId + "\" class=\"collapsyexpandy\" onclick=\"collapsyExpandy('" + currFindingId + "')\" title=\"" + tooltipText + "\"/></td>\n");
            } else {
                sbHtml.append("        <td><img src=\"WEB-OPAL/images/spacer.gif\" class=\"spacer\"/></td>\n");
            }
            sbHtml.append(this.getColumns("finding", dsFindingsForAnIncident, i));
            sbHtml.append("    </tr>\n");
            if (!hasChildren) continue;
            if (this.browser.isIE()) {
                sbHtml.append("    <tr class=\"" + (this.initiallyCollapsed ? "collapsedcontent" : "expandedcontent") + "\" id=\"content_" + currFindingId + "\">\n");
            } else {
                sbHtml.append("    <tr>\n");
            }
            sbHtml.append("        <td>&nbsp;</td>\n");
            int numColumns = ((PropertyListCollection)this.dsDisplayColumns.getObject(this.dsDisplayColumns.findRow("type", "finding"), "columns")).size();
            sbHtml.append("        <td colspan=\"" + numColumns + "\">\n");
            sbHtml.append("            <!-- START FINDING CHILDREN WRAPPER TABLE -->\n");
            if (this.browser.isIE()) {
                sbHtml.append("            <table border=0 cellpadding=\"3\" cellspacing=\"0\" summary=\"finding children wrapper table\">\n");
            } else {
                sbHtml.append("            <table class=\"" + (this.initiallyCollapsed ? "collapsedcontent" : "expandedcontent") + "\" id=\"content_" + currFindingId + "\" border=0 cellpadding=\"3\" cellspacing=\"0\" summary=\"finding children wrapper table\">\n");
            }
            if (dsPlansForThisFinding.size() > 0) {
                sbHtml.append("            <tr><td>\n");
                sbHtml.append(this.getPlanTable("finding", currFindingId, dsPlansForThisFinding));
                sbHtml.append("            </td></tr>\n");
            }
            if (dsWorkordersForThisFinding.size() > 0) {
                sbHtml.append("            <tr><td>\n");
                sbHtml.append(this.getWorkorderTable("finding", currFindingId, dsWorkordersForThisFinding));
                sbHtml.append("            </td></tr>\n");
            }
            sbHtml.append("        </table>\n");
            sbHtml.append("        <!-- END FINDING CHILDREN WRAPPER TABLE -->\n");
            sbHtml.append("    </td>\n");
            sbHtml.append("</tr>\n");
        }
        sbHtml.append("</table>\n");
        sbHtml.append("<!-- END FINDING TABLE -->\n");
        return sbHtml;
    }

    private StringBuffer getPlanTable(String parent, String parentId, DataSet dsPlansForAParent) {
        StringBuffer sbHtml = new StringBuffer("");
        sbHtml.append("<!-- START " + parent.toUpperCase() + " PLAN TABLE -->\n");
        sbHtml.append("<table id=\"content_" + parent + "_plans\" parentid=\"" + parentId + "\" border=0 cellpadding=\"3\" cellspacing=\"0\" summary=\"plan content table\">\n");
        boolean hasChildren = false;
        if (parent.equalsIgnoreCase("incident")) {
            hasChildren = this.dsIncident_ActionPlan_Workorder.size() > 0;
        } else if (parent.equalsIgnoreCase("finding")) {
            hasChildren = this.dsFinding_ActionPlan_Workorder.size() > 0;
        }
        for (int i = 0; i < dsPlansForAParent.size(); ++i) {
            if (hasChildren || !hasChildren && i == 0) {
                sbHtml.append("    <tr num_plans=" + dsPlansForAParent.size() + " >\n");
                sbHtml.append(this.getColumnHeaders("actionplan", true));
                sbHtml.append("    </tr>\n");
            }
            String currPlanId = dsPlansForAParent.getValue(i, "actionplanid");
            sbHtml.append("    <tr plan_index=" + (i + 1) + " planid=\"" + currPlanId + "\">\n");
            HashMap<String, String> hmFilter = new HashMap<String, String>();
            hmFilter.clear();
            hmFilter.clear();
            hmFilter.put("sourcesdcid", "LV_ActionPlan");
            hmFilter.put("sourcekeyid1", currPlanId);
            DataSet dsWorkordersForThisPlan = new DataSet();
            if (parent.equalsIgnoreCase("incident")) {
                dsWorkordersForThisPlan = this.dsIncident_ActionPlan_Workorder.getFilteredDataSet(hmFilter);
            } else if (parent.equalsIgnoreCase("finding")) {
                dsWorkordersForThisPlan = this.dsFinding_ActionPlan_Workorder.getFilteredDataSet(hmFilter);
            }
            hasChildren = dsWorkordersForThisPlan.size() > 0;
            String tooltipText = "";
            if (dsWorkordersForThisPlan.size() > 0) {
                tooltipText = tooltipText + dsWorkordersForThisPlan.size() + " Workorders ";
            }
            if (hasChildren) {
                sbHtml.append("        <td valign=\"middle\"><img ");
                if (this.initiallyCollapsed) {
                    sbHtml.append("src=\"WEB-OPAL/images/plus.gif\" currstate=\"collapsed\" ");
                } else {
                    sbHtml.append("src=\"WEB-OPAL/images/minus.gif\" currstate=\"expanded\" ");
                }
                sbHtml.append("id=\"img_" + currPlanId + "\" class=\"collapsyexpandy\" onclick=\"collapsyExpandy('" + currPlanId + "')\" title=\"" + tooltipText + "\"/></td>\n");
            } else {
                sbHtml.append("        <td><img src=\"WEB-OPAL/images/spacer.gif\" class=\"spacer\"/></td>\n");
            }
            sbHtml.append(this.getColumns("actionplan", dsPlansForAParent, i));
            sbHtml.append("    </tr>\n");
            if (!hasChildren) continue;
            if (this.browser.isIE()) {
                sbHtml.append("    <tr class=\"" + (this.initiallyCollapsed ? "collapsedcontent" : "expandedcontent") + "\" id=\"content_" + currPlanId + "\">\n");
            } else {
                sbHtml.append("    <tr>\n");
            }
            sbHtml.append("        <td>&nbsp;</td>\n");
            int numColumns = ((PropertyListCollection)this.dsDisplayColumns.getObject(this.dsDisplayColumns.findRow("type", "actionplan"), "columns")).size();
            sbHtml.append("        <td colspan=\"" + numColumns + "\">\n");
            sbHtml.append("            <!-- START PLAN CHILDREN WRAPPER TABLE -->\n");
            if (this.browser.isIE()) {
                sbHtml.append("            <table border=0 cellpadding=\"3\" cellspacing=\"0\" summary=\"plan children wrapper table\">\n");
            } else {
                sbHtml.append("            <table class=\"" + (this.initiallyCollapsed ? "collapsedcontent" : "expandedcontent") + "\" id=\"content_" + currPlanId + "\" border=0 cellpadding=\"3\" cellspacing=\"0\" summary=\"plan children wrapper table\">\n");
            }
            if (dsWorkordersForThisPlan.size() > 0) {
                sbHtml.append("            <tr><td>\n");
                sbHtml.append(this.getWorkorderTable("plan", currPlanId, dsWorkordersForThisPlan));
                sbHtml.append("            </td></tr>\n");
            }
            sbHtml.append("        </table>\n");
            sbHtml.append("        <!-- END FINDING CHILDREN WRAPPER TABLE -->\n");
            sbHtml.append("    </td>\n");
            sbHtml.append("</tr>\n");
        }
        sbHtml.append("</table>\n");
        sbHtml.append("<!-- END  " + parent.toUpperCase() + " PLAN TABLE -->\n");
        return sbHtml;
    }

    private StringBuffer getWorkorderTable(String parent, String parentId, DataSet dsWorkordersForAParent) {
        StringBuffer sbHtml = new StringBuffer("");
        sbHtml.append("<!-- START  " + parent.toUpperCase() + " WORKORDER TABLE -->\n");
        sbHtml.append("<table id=\"content_" + parent + "_workorders\" parentid=\"" + parentId + "\" border=0 cellpadding=\"3\" cellspacing=\"0\" summary=\"workorder content table\">\n");
        sbHtml.append("    <tr num_workorders=" + dsWorkordersForAParent.size() + " >\n");
        sbHtml.append(this.getColumnHeaders("workorder", true));
        sbHtml.append("    </tr>\n");
        for (int i = 0; i < dsWorkordersForAParent.size(); ++i) {
            String currWorkorderId = dsWorkordersForAParent.getValue(i, "workorderid");
            sbHtml.append("    <tr workorder_index=" + (i + 1) + " workorderid=\"" + currWorkorderId + "\">\n");
            sbHtml.append("        <td><img src=\"WEB-OPAL/images/spacer.gif\" class=\"spacer\"/></td>\n");
            sbHtml.append(this.getColumns("workorder", dsWorkordersForAParent, i));
            sbHtml.append("    </tr>\n");
        }
        sbHtml.append("</table>\n");
        sbHtml.append("<!-- END  " + parent.toUpperCase() + " WORKORDER TABLE -->\n");
        return sbHtml;
    }
}

