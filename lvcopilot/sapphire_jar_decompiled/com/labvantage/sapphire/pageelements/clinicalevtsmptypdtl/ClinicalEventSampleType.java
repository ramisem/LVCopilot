/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.clinicalevtsmptypdtl;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.pageelements.controls.TabGroup;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ClinicalEventSampleType
extends BaseElement {
    public static final String LABVANTAGE_CVS_ID = "$Revision: 104173 $";
    static final String PROPERTYHANDLERCLASS = "com.labvantage.sapphire.pageelements.clinicalevtsmptypdtl.ClinicalEventSampleTypePropertyHandler";
    static final String PARENTDATASET = "primary";
    static final String PROPERTY_SHOWPROGRESS = "showprogress";
    static final String PROPERTY_VIEWONLY = "viewonly";
    static final String PREFIX_SAMPLETYPENAME = "cpeventsampletype";
    static final String PREFIX_SPECIMENDEFNAME = "cpeventstspecimendef";
    static final String PREFIX_ASSAYTYPENAME = "cpeventstassaytype";
    static final String IMG_SRC_ADD = "WEB-CORE/images/gif/AddRow.gif";
    static final String IMG_SRC_DEL = "WEB-CORE/images/png/RemoveRow.png";
    static final String IMG_SRC_COLLAPSED = "WEB-CORE/elements/images/plus.gif";
    static final String IMG_SRC_EXPANDED = "WEB-CORE/elements/images/minus.gif";
    static final String CSS_ELEMENTTABLE = "";
    static final String CSS_STTABLE = "";
    static final String CSS_STDETAILDIV = "";
    private boolean viewOnly;
    private boolean showProgress;
    private HashMap<String, LinkedHashSet<String>> hsSTAT = new HashMap();
    private HashMap<String, LinkedHashSet<String>> hsATWI = new HashMap();
    private HashMap<String, LinkedHashSet<String>> hsSTCT = new HashMap();
    private TranslationProcessor tp = null;

    private boolean loadProperties() {
        boolean returnVal = true;
        this.logger.debug("loadProperties called...");
        String temp = this.element.getProperty(PROPERTY_VIEWONLY);
        this.viewOnly = temp != null && temp.length() != 0 && temp.equalsIgnoreCase("y");
        boolean lockedByOther = this.checkLockState(PARENTDATASET);
        if (lockedByOther) {
            this.viewOnly = true;
        }
        this.logger.debug("viewOnly = " + this.viewOnly);
        temp = this.element.getProperty(PROPERTY_SHOWPROGRESS);
        this.showProgress = temp != null && temp.length() != 0 && temp.equalsIgnoreCase("y");
        this.logger.debug("showProgress = " + this.showProgress);
        return returnVal;
    }

    private DataSet getCPEventSampleTypeData(String eventDefId) {
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT s_sampletypeid ");
        sql.append("FROM s_eventdefsampletype ");
        sql.append("WHERE s_eventdefid = ").append(safeSQL.addVar(eventDefId)).append(" ");
        sql.append("ORDER BY usersequence");
        return this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    private DataSet getCPEventSTSpecimenDefData(String eventDefId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT cpest.s_sampletypeid, cpestat.s_specimendefid, cpestat.specimentype, cpestat.usersequence, ");
        sql.append("cpestat.quantity, cpestat.specimenlabel, cpestat.volume, cpestat.volumeunits, cpestat.childsampleplanid, cpestat.childsampleplanversionid ");
        PropertyListCollection columns = this.element.getCollectionNotNull("specimentypecolumns");
        for (int col = 0; col < columns.size(); ++col) {
            String columnid;
            PropertyList column = columns.getPropertyList(col);
            if (column == null || StringUtil.getLen(columnid = column.getProperty("column", "")) <= 0L) continue;
            if (columnid.startsWith("(")) {
                columnid = StringUtil.replaceAll(columnid, "s_eventdefstspecimendef.", "cpestat.");
                sql.append(", ").append(columnid);
                continue;
            }
            sql.append(", cpestat.").append(columnid);
            if (!"lookupversioned".equals(column.getProperty("mode"))) continue;
            sql.append(", cpestat.").append(column.getProperty("versioncolumnid"));
        }
        SafeSQL safeSQL = new SafeSQL();
        sql.append(" FROM s_eventdefsampletype cpest, s_eventdefstspecimendef cpestat ");
        sql.append(" WHERE cpest.s_eventdefid = ").append(safeSQL.addVar(eventDefId)).append(" ");
        sql.append(" AND cpest.s_eventdefid = cpestat.s_eventdefid ");
        sql.append(" AND cpest.s_sampletypeid = cpestat.s_sampletypeid ");
        sql.append(" ORDER BY cpest.usersequence, cpestat.usersequence");
        return this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    private DataSet getCPEventSTAssayTypeData(String eventDefId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT cpest.s_sampletypeid, cpestat.s_assaytypeid, cpestat.s_mapid, cpestat.arrivalorder, ");
        sql.append("cpestat.specimentype, cpestat.workitemid, cpestat.usersequence ");
        PropertyListCollection columns = this.element.getCollectionNotNull("assaytypecolumns");
        for (int col = 0; col < columns.size(); ++col) {
            String columnid;
            PropertyList column = columns.getPropertyList(col);
            if (column == null || StringUtil.getLen(columnid = column.getProperty("column", "")) <= 0L) continue;
            sql.append(", cpestat.").append(columnid);
        }
        SafeSQL safeSQL = new SafeSQL();
        sql.append(" FROM s_eventdefsampletype cpest, s_eventdefstatmap cpestat ");
        sql.append(" WHERE cpest.s_eventdefid = ").append(safeSQL.addVar(eventDefId)).append(" ");
        sql.append(" AND cpest.s_eventdefid = cpestat.s_eventdefid ");
        sql.append(" AND cpest.s_sampletypeid = cpestat.s_sampletypeid ");
        sql.append(" ORDER BY cpest.usersequence, cpestat.usersequence");
        return this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    private DataSet getAssayTypeWorkItems(String eventDefId, String uniqueSampleTypes) {
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT cpatst.s_sampletypeid, cpatst.s_assaytypeid, atwi.workitemid ");
        sql.append("FROM s_eventdef edef, s_cpassaytypesampletype cpatst, s_assaytypeworkitem atwi ");
        sql.append("WHERE edef.s_eventdefid = ").append(safeSQL.addVar(eventDefId)).append(" ");
        sql.append("AND edef.clinicalprotocolid = cpatst.s_clinicalprotocolid ");
        sql.append("AND edef.clinicalprotocolversionid = cpatst.s_clinicalprotocolversionid ");
        sql.append("AND edef.clinicalprotocolrevision = cpatst.s_clinicalprotocolrevision ");
        sql.append("AND cpatst.s_sampletypeid IN (").append(safeSQL.addIn(uniqueSampleTypes, ";")).append(") ");
        sql.append("AND cpatst.s_assaytypeid = atwi.s_assaytypeid ");
        sql.append("ORDER BY cpatst.s_sampletypeid, cpatst.s_assaytypeid, atwi.workitemid");
        return this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    private DataSet getContainerTypesBySampleTypes(String uniqueSampleTypes) {
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT stct.s_sampletypeid, stct.containertypeid ");
        sql.append("FROM s_sampletypecontainertype stct ");
        sql.append("WHERE stct.s_sampletypeid IN (").append(safeSQL.addIn(uniqueSampleTypes, ";")).append(") ");
        sql.append("ORDER BY stct.s_sampletypeid, stct.containertypeid");
        return this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    private DataSet getArrivalOrders() {
        return this.getQueryProcessor().getRefTypeDataSet("ArrivalOrder");
    }

    @Override
    public String getHtml() {
        String eventDefId = this.requestContext.getPropertyList("pagedata").getProperty("keyid1");
        StringBuilder html = new StringBuilder();
        StringBuilder jsonHtml = new StringBuilder();
        this.tp = this.getTranslationProcessor();
        if (this.loadProperties()) {
            DataSet cpEventSampleTypeData = this.getCPEventSampleTypeData(eventDefId);
            DataSet cpEventSTSpecimenDefData = this.getCPEventSTSpecimenDefData(eventDefId);
            for (int i = 0; i < cpEventSTSpecimenDefData.size(); ++i) {
                if (!OpalUtil.isNotEmpty(cpEventSTSpecimenDefData.getString(i, "childsampleplanid", "")) || !OpalUtil.isEmpty(cpEventSTSpecimenDefData.getValue(i, "childsampleplanversionid", ""))) continue;
                cpEventSTSpecimenDefData.setString(i, "childsampleplanversionid", "C");
            }
            DataSet cpEventSTAssayTypeData = this.getCPEventSTAssayTypeData(eventDefId);
            DataSet dsSampleTypeAssayTypeWorkItem = null;
            DataSet dsSampleTypeContainerType = null;
            if (cpEventSampleTypeData.getRowCount() > 0) {
                String uniqueSampleTypes = cpEventSampleTypeData.getColumnValues("s_sampletypeid", ";");
                dsSampleTypeAssayTypeWorkItem = this.getAssayTypeWorkItems(eventDefId, uniqueSampleTypes);
                dsSampleTypeContainerType = this.getContainerTypesBySampleTypes(uniqueSampleTypes);
                dsSampleTypeContainerType.showData();
            }
            jsonHtml.append("var jsonClinicalEvtSmpTypDtl = {\"sampletypearray\" : [");
            html.append(JavaScriptAPITag.getJQueryAPI(true, false, null, this.pageContext));
            if ("Timepoint".equals(OpalUtil.getColumnValue(this.getQueryProcessor(), "s_eventdef", "eventdeftype", "s_eventdefid = ?", new String[]{eventDefId}))) {
                html.append("\n<script>");
                html.append("\nvar detail_validations = new Array();");
                html.append("\nfunction validatedetails() {");
                html.append("    var returnvalue = '';");
                html.append("    var valifield = '';");
                html.append("    var invalidfields = '';");
                html.append("    var size = detail_validations.length;");
                html.append("    for ( x = 0; x < size; x++ ) {");
                html.append("        var validation = detail_validations[x].split( '||' );");
                html.append("        var afieldid = validation[0];");
                html.append("        var afieldel = document.getElementById( afieldid );");
                html.append("        valifield = validateField( afieldid, validation[1], afieldel );");
                html.append("        if ( valifield != '' ) {");
                html.append("            invalidfields += ';' + valifield;");
                html.append("        }");
                html.append("    }");
                html.append("    if ( invalidfields.length > 0 ) {");
                html.append("        handleValidationResult( invalidfields.substring( 1 ) );");
                html.append("        returnvalue = invalidfields.substring( 1 );");
                html.append("    }");
                html.append("    return returnvalue;");
                html.append("}\n");
                html.append("</script>\n");
            }
            html.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/clinicalevtsmptypdtl/scripts/clinicaleventsampletype.js\"></script>");
            html.append("<input type='hidden' name='__propertyhandler_").append(this.elementid).append("' value='").append(PROPERTYHANDLERCLASS).append("'/>");
            html.append("<input type=hidden id=\"s_eventdefid_").append(this.elementid).append("\" name=\"s_eventdefid_").append(this.elementid).append("\" value=\"").append(eventDefId).append("\">");
            html.append("<input type=hidden id=\"__submitjsonstring_").append(this.elementid).append("\" name=\"__submitjsonstring_").append(this.elementid).append("\" value=\"\">");
            html.append("<input type=hidden id=\"__servicearray_").append(this.elementid).append("\" name=\"__servicearray_").append(this.elementid).append("\" value=\"\">");
            html.append("<table cellpadding=0 cellspacing=0 width=\"100%\" id=\"").append(this.elementid).append("_table\" border=0 class=\"");
            html.append("").append("\"><tr><td nowrap valign=top>");
            if (!this.viewOnly) {
                this.renderButton(html, "cpeventsampletype_add", this.tp.translate("Add SampleType"), "cpEventSampleType.addSampleType();");
                html.append("</td></tr><tr><td nowrap>");
            }
            this.renderSampleTypeData(html, jsonHtml, cpEventSampleTypeData, cpEventSTSpecimenDefData, cpEventSTAssayTypeData, dsSampleTypeAssayTypeWorkItem, dsSampleTypeContainerType);
            jsonHtml.append("]};");
            html.append("</td></tr></table>");
            html.append("<script language=\"JavaScript\">");
            html.append(jsonHtml.toString());
            html.append("cpEventSampleType.sEventDefId = '").append(eventDefId).append("';");
            html.append("cpEventSampleType.sElementId = '").append(this.elementid).append("';");
            html.append("cpEventSampleType.bShowProgressDialog = ").append(this.showProgress).append(";");
            html.append("cpEventSampleType.sJSONFieldId = '__submitjsonstring_").append(this.elementid).append("';");
            html.append("document.getElementById(cpEventSampleType.sJSONFieldId).value = arrayToJSONString(jsonClinicalEvtSmpTypDtl.sampletypearray);");
            PropertyListCollection columns = this.element.getCollectionNotNull("specimentypecolumns");
            for (int col = 0; col < columns.size(); ++col) {
                PropertyList column = columns.getPropertyList(col);
                if (column == null || "hidden".equals(column.getProperty("mode"))) continue;
                String columnid = column.getProperty("column", "");
                String defaultVal = column.getProperty("default", "");
                if (defaultVal.length() <= 0) continue;
                html.append("cpEventSampleType.oJSONOBJSDTEMPLATE." + columnid + "='" + defaultVal + "';");
            }
            html.append("</script>");
        }
        return html.toString();
    }

    private void renderSampleTypeData(StringBuilder html, StringBuilder jsonHtml, DataSet cpEventSampleTypeData, DataSet cpEventSTSpecimenDefData, DataSet cpEventSTAssayTypeData, DataSet dsSampleTypeAssayTypeWorkItem, DataSet dsSampleTypeContainerType) {
        LinkedHashSet<String> temp;
        String sampleTypeId2;
        HashMap<String, String> filterMap = new HashMap<String, String>();
        html.append("<script type=\"text/javascript\">");
        html.append("var saSampleTypeAssayType = new Array();");
        html.append("var saSampleTypeAssayTypeUsed = new Array();");
        html.append("var saSampleTypeAssayTypeUnused = new Array();");
        html.append("var saAssayTypeWorkItem = new Array();");
        html.append("var saSampleTypeContainerType = new Array();");
        if (dsSampleTypeAssayTypeWorkItem != null && dsSampleTypeAssayTypeWorkItem.getRowCount() > 0) {
            String val;
            Iterator iterator;
            LinkedHashSet<String> vals;
            String workItemId;
            String assayTypeId2;
            for (int rInd = 0; rInd < dsSampleTypeAssayTypeWorkItem.getRowCount(); ++rInd) {
                sampleTypeId2 = dsSampleTypeAssayTypeWorkItem.getValue(rInd, "s_sampletypeid");
                assayTypeId2 = dsSampleTypeAssayTypeWorkItem.getValue(rInd, "s_assaytypeid");
                workItemId = dsSampleTypeAssayTypeWorkItem.getValue(rInd, "workitemid");
                temp = this.hsSTAT.containsKey(sampleTypeId2) ? this.hsSTAT.get(sampleTypeId2) : new LinkedHashSet();
                temp.add(assayTypeId2);
                this.hsSTAT.put(sampleTypeId2, temp);
                temp = this.hsATWI.containsKey(assayTypeId2) ? this.hsATWI.get(assayTypeId2) : new LinkedHashSet();
                temp.add(workItemId);
                this.hsATWI.put(assayTypeId2, temp);
            }
            Set<String> keys = this.hsSTAT.keySet();
            for (String sampleTypeId2 : keys) {
                String assayTypeIds = "";
                vals = this.hsSTAT.get(sampleTypeId2);
                iterator = vals.iterator();
                while (iterator.hasNext()) {
                    assayTypeId2 = val = (String)iterator.next();
                    if (assayTypeIds.length() > 0) {
                        assayTypeIds = assayTypeIds + ";";
                    }
                    assayTypeIds = assayTypeIds + assayTypeId2;
                }
                html.append("saSampleTypeAssayType['").append(sampleTypeId2).append("'] = '").append(assayTypeIds).append("';");
            }
            keys = this.hsATWI.keySet();
            for (String assayTypeId2 : keys) {
                String workItemIds = "";
                vals = this.hsATWI.get(assayTypeId2);
                iterator = vals.iterator();
                while (iterator.hasNext()) {
                    workItemId = val = (String)iterator.next();
                    if (workItemIds.length() > 0) {
                        workItemIds = workItemIds + ";";
                    }
                    workItemIds = workItemIds + workItemId;
                }
                html.append("saAssayTypeWorkItem['").append(assayTypeId2).append("'] = '").append(workItemIds).append("';");
            }
        }
        if (dsSampleTypeContainerType != null && dsSampleTypeContainerType.getRowCount() > 0) {
            String containerTypeId;
            for (int rInd = 0; rInd < dsSampleTypeContainerType.getRowCount(); ++rInd) {
                sampleTypeId2 = dsSampleTypeContainerType.getValue(rInd, "s_sampletypeid");
                containerTypeId = dsSampleTypeContainerType.getValue(rInd, "containertypeid");
                LinkedHashSet<String> temp2 = this.hsSTCT.containsKey(sampleTypeId2) ? this.hsSTCT.get(sampleTypeId2) : new LinkedHashSet<String>();
                temp2.add(containerTypeId);
                this.hsSTCT.put(sampleTypeId2, temp2);
            }
            Set<String> keys = this.hsSTCT.keySet();
            temp = keys.iterator();
            while (temp.hasNext()) {
                String key;
                sampleTypeId2 = key = temp.next();
                String containerTypeIds = "";
                LinkedHashSet<String> vals = this.hsSTCT.get(sampleTypeId2);
                Iterator iterator = vals.iterator();
                while (iterator.hasNext()) {
                    String val;
                    containerTypeId = val = (String)iterator.next();
                    if (containerTypeIds.length() > 0) {
                        containerTypeIds = containerTypeIds + ";";
                    }
                    containerTypeIds = containerTypeIds + containerTypeId;
                }
                html.append("saSampleTypeContainerType['").append(sampleTypeId2).append("'] = '").append(containerTypeIds).append("';");
            }
        }
        html.append("</script>");
        html.append("<table cellpadding=0 cellspacing=0 width=\"100%\" id=\"").append(PREFIX_SAMPLETYPENAME).append("_table\" border=0 class=\"");
        html.append("").append("\"><tbody>");
        if (cpEventSampleTypeData.getRowCount() > 0) {
            ConfigurationProcessor configurationProcessor = this.getConfigurationProcessor();
            for (int i = 0; i < cpEventSampleTypeData.getRowCount(); ++i) {
                String cpEventSampleTypeId = cpEventSampleTypeData.getValue(i, "s_sampletypeid");
                if (i != 0) {
                    jsonHtml.append(",");
                }
                jsonHtml.append("{\"strowindex\" : \"").append(i).append("\",");
                jsonHtml.append("\"s_sampletypeid\" : \"").append(cpEventSampleTypeId).append("\",");
                jsonHtml.append("\"stateflag\" : \"U\",");
                jsonHtml.append("\"specimendefarray\" : [");
                filterMap.clear();
                filterMap.put("s_sampletypeid", cpEventSampleTypeId);
                DataSet filteredSpecimenDefs = cpEventSTSpecimenDefData.getFilteredDataSet(filterMap);
                int specimenDefCount = filteredSpecimenDefs.getRowCount();
                DataSet filteredAssayTypes = cpEventSTAssayTypeData.getFilteredDataSet(filterMap);
                int assayTypeCount = filteredAssayTypes.getRowCount();
                String divid = "cpeventsampletype_div_" + i;
                boolean expanded = "Y".equals(configurationProcessor.getProfileProperty("userconfig_" + divid + "_expanded"));
                html.append("<tr id=\"").append(PREFIX_SAMPLETYPENAME).append("_row_").append(i).append("\" class='sampleTypeHeader' style='cursor:pointer'>");
                html.append("<td nowrap style='padding:6px;padding-right:240px;' onclick=\"cpEventSampleType.expandCollapseSampleType(").append(i).append(", '").append(divid).append("');\">");
                html.append("&nbsp;<img src=\"").append(expanded ? IMG_SRC_EXPANDED : IMG_SRC_COLLAPSED).append("\" title=\"Expand\" border=0 id='").append(this.elementid).append("_toggle_").append(i).append("'>");
                html.append("&nbsp;<b>").append(cpEventSampleTypeId).append("</b>&nbsp;[").append(this.tp.translate("Specimens")).append(":&nbsp;").append(specimenDefCount);
                html.append(",&nbsp;").append(this.tp.translate("Assay Types")).append(":&nbsp;").append(assayTypeCount).append("]");
                html.append("</td>");
                html.append("<td width=\"20px\">");
                if (this.viewOnly) {
                    html.append("&nbsp;");
                } else {
                    html.append("<img style=\"cursor: pointer;\" src=\"").append(IMG_SRC_DEL).append("\" title=\"Delete\" border=0 onclick=\"");
                    html.append("cpEventSampleType.deleteSampleType(").append(i).append(");\">");
                }
                html.append("</td>");
                html.append("</tr>");
                html.append("<tr><td nowrap colspan=\"2\">");
                html.append("<div id=\"").append(divid);
                html.append("\" class=\"").append("").append("\" style=\"display:").append(expanded ? "block" : "none").append(";padding:0;margin:0;border:1px solid lightgray;\">");
                Tab specimenTab = this.renderSpecimenDefData(jsonHtml, filteredSpecimenDefs, cpEventSampleTypeId, i);
                jsonHtml.append("],");
                jsonHtml.append("\"assaytypearray\" : [");
                Tab assayTab = this.renderAssayTypeData(html, jsonHtml, filteredAssayTypes, cpEventSampleTypeId, i);
                jsonHtml.append("]}");
                TabGroup tabGroup = new TabGroup();
                tabGroup.setTab(specimenTab);
                tabGroup.setTab(assayTab);
                tabGroup.setId(this.elementid + "_tabgroup_" + i);
                tabGroup.setPageContext(this.pageContext);
                html.append("<div style='padding:4px'>").append(tabGroup.getTabGroupHtml()).append("</div>");
                html.append("</div>");
                html.append("</td></tr>");
            }
        }
        html.append("</tbody></table>");
    }

    private Tab renderSpecimenDefData(StringBuilder jsonHtml, DataSet filteredSpecimenDefs, String sampletypeid, int sampleTypeIndex) {
        StringBuilder sb = new StringBuilder();
        JSONArray jsonArray = new JSONArray();
        String stagPrefix = "cpeventsampletype_" + sampleTypeIndex + "_" + PREFIX_SPECIMENDEFNAME + "_";
        sb.append("<style type='text/css'>");
        sb.append("\nspan.applyonadd{border:1px solid #999;background:#339900;padding: 1px 4px;;margin:1px;color:#fff;border-radius: 5px;font-size:10px;cursor:pointer;}");
        sb.append("\nspan.onlyadd{border:1px solid #999;background:#eee;padding: 1px 4px;margin:1px;border-radius: 5px;font-size: 10px;cursor:pointer;}");
        sb.append("\ntd.oicon {padding:0 1px;}");
        sb.append("\n.sort-highlight{background: #cfeff7; height: 24px;}");
        sb.append("\n.sort-highlight > td{border-top:1px solid #5c97bf;border-bottom:1px solid #5c97bf;}");
        sb.append("\n.sort-highlight > td:first-child{border-left:1px solid #5c97bf;}");
        sb.append("\n.sort-highlight > td:last-child{border-right:1px solid #5c97bf;}");
        sb.append("\n.typeaheadfield {border:1px solid #4daf7c;}");
        sb.append("\n</style>");
        sb.append("<table cellpadding=0 cellspacing=0 border=0><tr><td nowrap>");
        sb.append("<table width=100% cellpadding=2 cellspacing=0 id='").append(stagPrefix).append("table' border=0 class=maintform_table><tbody>");
        int columnCount = 5;
        sb.append("<tr class=maintform_tablehead>");
        PropertyListCollection columns = this.element.getCollectionNotNull("specimentypecolumns");
        for (int col = 0; col < columns.size(); ++col) {
            PropertyList column = columns.getPropertyList(col);
            if (column == null || "hidden".equals(column.getProperty("mode"))) continue;
            String columnid = column.getProperty("column", "");
            sb.append("<td nowrap width=\"").append(column.getProperty("width", "120")).append("\" class=maintform_fieldtitle align='").append(column.getProperty("align", "left")).append("' style='padding:4px'><b>");
            sb.append(this.tp.translate(column.getProperty("title", columnid))).append("</b></td>");
            ++columnCount;
        }
        sb.append("<td nowrap width=\"30\" class=maintform_fieldtitle>&nbsp;</td>");
        sb.append("</tr>");
        String workitemviewpage = "rc?command=page&page=WorkItemView";
        String workitemaddpage = "rc?command=page&page=ServiceLookupMulti";
        String privateplanmaintpage = "rc?command=page&page=LV_ProtectedSamplePlanMaint";
        String eventDefId = this.requestContext.getPropertyList("pagedata").getProperty("keyid1");
        if (filteredSpecimenDefs.getRowCount() > 0) {
            for (int row = 0; row < filteredSpecimenDefs.size(); ++row) {
                PropertyList column;
                int col;
                String cpEventSpecimenDefId = filteredSpecimenDefs.getValue(row, "s_specimendefid");
                if (row != 0) {
                    jsonHtml.append(",");
                }
                jsonHtml.append("{\"agrowindex\" : \"").append(row).append("\",");
                jsonHtml.append("\"s_specimendefid\" : \"").append(cpEventSpecimenDefId).append("\",");
                boolean flag = true;
                for (col = 0; col < columns.size(); ++col) {
                    String versioncolumnid;
                    String columnid;
                    column = columns.getPropertyList(col);
                    if (column == null || (columnid = column.getProperty("column", "").trim()).length() <= 0) continue;
                    int index = columnid.lastIndexOf(" ");
                    if (index != -1) {
                        columnid = columnid.substring(index).trim();
                    }
                    String value = StringUtil.replaceAll(filteredSpecimenDefs.getValue(row, columnid, ""), "\n", "\\n");
                    value = StringUtil.replaceAll(value, "\"", "&quot;");
                    jsonHtml.append("\"").append(columnid).append("\" : \"").append(value).append("\",");
                    if ("lookupversioned".equals(column.getProperty("mode", "")) && (versioncolumnid = column.getProperty("versioncolumnid", "").trim()).length() > 0) {
                        jsonHtml.append("\"").append(versioncolumnid).append("\" : \"").append(filteredSpecimenDefs.getValue(row, versioncolumnid, "")).append("\",");
                    }
                    if (!columnid.equalsIgnoreCase("usersequence")) continue;
                    flag = false;
                }
                if (flag) {
                    jsonHtml.append("\"usersequence\" : \"").append(filteredSpecimenDefs.getValue(row, "usersequence")).append("\",");
                }
                jsonHtml.append("\"stateflag\" : \"U\"}");
                sb.append("<tr id=\"").append(stagPrefix).append("row_").append(row).append("\">");
                for (col = 0; col < columns.size(); ++col) {
                    String mode;
                    column = columns.getPropertyList(col);
                    if (column == null || "hidden".equals(mode = column.getProperty("mode"))) continue;
                    sb.append("<td nowrap align=\"").append(column.getProperty("align", "left")).append("\" class=maintform_field style='vertical-align:top'>");
                    if ("workitemlookup".equals(mode)) {
                        PropertyList workitemprops = column.getPropertyListNotNull("workitemprops");
                        privateplanmaintpage = workitemprops.getProperty("privateplanmaintpage", "rc?command=page&page=LV_ProtectedSamplePlanMaint");
                        String displayformat = workitemprops.getProperty("displayformat", "[workitemid] (v[workitemversionid])");
                        String[] displaytokens = StringUtil.getTokens(displayformat);
                        StringBuilder sql = new StringBuilder();
                        sql.append("select ed.workitemid, ed.workitemversionid, ed.workiteminstance, ed.applyonaddflag, ed.usersequence, wi.embedchildsampleplanid, wi.embedchildsampleplanversionid, wi.supportembeddedchildplanflag,");
                        sql.append(" ed.embedchildsampleplanid childsampleplanid, ed.embedchildsampleplanversionid childsampleplanversionid, wi.workitemversionid viewversionid, ed.assigneddepartmentid");
                        for (String token : displaytokens) {
                            if ("workitemid".equals(token) || "workitemversionid".equals(token)) continue;
                            if (token.startsWith("workitem.")) {
                                sql.append(", (select ").append(token).append(" from workitem where workitem.workitemid = wi.workitemid and workitem.workitemversionid = wi.workitemversionid) ").append(StringUtil.replaceAll(token, ".", "_"));
                                continue;
                            }
                            sql.append(", ").append(token).append(" ").append(StringUtil.replaceAll(token, ".", "_"));
                        }
                        sql.append(" from s_eventdefstspecimendefwi ed, workitem wi");
                        sql.append(" where ed.s_eventdefid = ?");
                        sql.append(" and ed.s_specimendefid = ?");
                        sql.append(" and ed.s_sampletypeid = ?");
                        sql.append(" and wi.workitemid = ed.workitemid");
                        sql.append(" AND ((ed.WORKITEMVERSIONID is null and wi.VERSIONSTATUS = 'C') or (ed.WORKITEMVERSIONID is not null and wi.WORKITEMVERSIONID = ed.WORKITEMVERSIONID))");
                        sql.append(" order by ed.usersequence");
                        DataSet wids = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{eventDefId, cpEventSpecimenDefId, sampletypeid});
                        sb.append("<table cellpadding=4 cellspacing=0 border=0 style='width:100%' name='serviceDataContainer'><tr>");
                        sb.append("<td style='vertical-align:top'>");
                        sb.append("<table cellpadding=4 cellspacing=0 border=0 style='width:100%;' class='serviceManageTable maintform_table'");
                        sb.append(" eventdefid=\"").append(eventDefId).append("\"");
                        sb.append(" specimendefid=\"").append(cpEventSpecimenDefId).append("\"");
                        sb.append(" sampletypeid=\"").append(sampletypeid).append("\"");
                        sb.append(" id=\"servicetable_").append(sampleTypeIndex).append("_").append(row).append("\">");
                        PropertyList headertitle = workitemprops.getPropertyListNotNull("headertitle");
                        sb.append("<tr>");
                        sb.append("<td class='maintform_fieldtitle'>").append(this.tp.translate(headertitle.getProperty("Service", "Service"))).append("</td>");
                        sb.append("<td class='maintform_fieldtitle'>").append(this.tp.translate(headertitle.getProperty("Apply", "Apply"))).append("</td>");
                        sb.append("<td class='maintform_fieldtitle'>").append(this.tp.translate(headertitle.getProperty("Department", "Department"))).append("</td>");
                        sb.append("<td class='maintform_fieldtitle'>&nbsp;</td>");
                        sb.append("</tr>");
                        boolean editedflag = false;
                        if (wids != null && wids.size() > 0) {
                            String departmentrestrictivewhere = workitemprops.getProperty("departmentrestrictivewhere", "");
                            if (OpalUtil.isNotEmpty(departmentrestrictivewhere)) {
                                String[] dtokens;
                                for (String token : dtokens = StringUtil.getTokens(departmentrestrictivewhere)) {
                                    if (!"sysuserid".equals(token)) continue;
                                    departmentrestrictivewhere = StringUtil.replaceAll(departmentrestrictivewhere, "[" + token + "]", this.connectionInfo.getSysuserId());
                                }
                            }
                            for (int i = 0; i < wids.size(); ++i) {
                                String widisplay = displayformat;
                                workitemviewpage = column.getPropertyListNotNull("link").getProperty("href", "rc?command=page&page=WorkItemView");
                                workitemaddpage = column.getPropertyListNotNull("workitemprops").getProperty("addpage", "rc?command=page&page=ServiceLookupMulti");
                                String workitemid = wids.getString(i, "workitemid");
                                String workitemversionid = wids.getValue(i, "workitemversionid", "C");
                                String workiteminstance = wids.getValue(i, "workiteminstance");
                                String privatechildsampleplanid = wids.getString(i, "childsampleplanid", "");
                                String privatechildsampleplanversionid = wids.getString(i, "childsampleplanversionid", "");
                                String assigneddepartmentid = wids.getString(i, "assigneddepartmentid", "");
                                sb.append("<tr workitemid=\"").append(workitemid).append("\" workitemversionid=\"").append(workitemversionid).append("\" workiteminstance=\"").append(workiteminstance).append("\"");
                                sb.append(" embedchildsampleplanid=\"").append(privatechildsampleplanid).append("\"");
                                sb.append(" embedchildsampleplanversionid=\"").append(privatechildsampleplanversionid).append("\"");
                                sb.append(" viewversionid=\"").append(wids.getString(i, "viewversionid")).append("\">");
                                sb.append("<td NOWRAP idcell=\"Y\" width=\"*\" class='maintform_field'>");
                                if ("C".equals(workitemversionid)) {
                                    sql.setLength(0);
                                    sql.append("select ");
                                    for (String token : displaytokens) {
                                        if (token.startsWith("workitem.")) {
                                            sql.append(token).append(" ").append(StringUtil.replaceAll(token, ".", "_")).append(",");
                                            continue;
                                        }
                                        sql.append("workitem.").append(token).append(",");
                                    }
                                    sql.append("usersequence from workitem where workitemid = ? and versionstatus = 'C'");
                                    DataSet tokends = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{workitemid});
                                    String[] stringArray = displaytokens;
                                    int n = stringArray.length;
                                    for (int token = 0; token < n; ++token) {
                                        String token2 = stringArray[token];
                                        widisplay = token2.endsWith("workitemversionid") ? StringUtil.replaceAll(widisplay, "[" + token2 + "]", "C") : StringUtil.replaceAll(widisplay, "[" + token2 + "]", tokends.getValue(0, StringUtil.replaceAll(token2, ".", "_"), ""));
                                    }
                                } else {
                                    for (String token : displaytokens) {
                                        widisplay = StringUtil.replaceAll(widisplay, "[" + token + "]", wids.getValue(i, StringUtil.replaceAll(token, ".", "_"), ""));
                                    }
                                }
                                sb.append("<a href=\"#\" onclick=\"cpEventSampleType.viewWorkItem()\"'>");
                                sb.append(widisplay);
                                if (privatechildsampleplanid.length() != 0) {
                                    editedflag = true;
                                    sb.append("<span style='color:black;font-weight:bold'><sup>#</sup></span>");
                                }
                                sb.append("</a>");
                                sb.append("</td>");
                                sb.append("<td class='oicon maintform_field' style='width:20px;text-align:center;'>");
                                sb.append("<input type=checkbox title=\"Apply on Add\" ");
                                sb.append("Y".equals(wids.getString(i, "applyonaddflag", "N")) ? "checked" : "");
                                sb.append(" onclick='cpEventSampleType.resetServiceData()'");
                                sb.append(this.viewOnly ? " disabled" : "").append(">");
                                sb.append("</td>");
                                sb.append("<td class='oicon maintform_field' style='width:120px;'>");
                                if (this.viewOnly) {
                                    sb.append(assigneddepartmentid);
                                } else {
                                    String id = StringUtil.replaceAll(UUID.randomUUID().toString(), "-", "0");
                                    sb.append("<input name='departmentinput' id=\"").append(id).append("\" onkeyup=\"showSuggestion()\" onchange=\"cpEventSampleType.resetServiceData()\"");
                                    sb.append(" value='").append(assigneddepartmentid).append("' style='width:120px;' readonly title=\"").append(this.getTranslationProcessor().translate("Assigned Department")).append("\" class=\"typeaheadfield\">");
                                    sb.append("<script>var oLUPD_").append(id).append(" = {\"selectortype\":\"\",\"sdcid\":\"Department\", restrictivewhere: \"").append(departmentrestrictivewhere).append("\"};</script>");
                                }
                                sb.append("</td>");
                                if (!this.viewOnly) {
                                    sb.append("<td class='oicon maintform_field' align='right'>");
                                    if (!"Y".equals(wids.getString(i, "supportembeddedchildplanflag", ""))) {
                                        sb.append("&nbsp;");
                                    } else if (privatechildsampleplanid.length() == 0) {
                                        sb.append("<a href='#Add' onclick=\"cpEventSampleType.addPrivateChildSamplePlan()\">Edit</a>");
                                    } else {
                                        sb.append("<a href='#Edit' onclick=\"cpEventSampleType.managePrivateChildSamplePlan()\">Edit</a>");
                                    }
                                    sb.append("&nbsp;<img src='rc?command=image&image=FlatBlackTrash&size=16' style='width:12px;height:12px;cursor: pointer;' onclick=\"cpEventSampleType.removeService(this)\" title=\"").append(this.tp.translate("Remove")).append("\">");
                                    sb.append("</td>");
                                }
                                sb.append("</tr>");
                                try {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("eventdefid", eventDefId);
                                    jsonObject.put("specimendefid", cpEventSpecimenDefId);
                                    jsonObject.put("sampletypeid", sampletypeid);
                                    jsonObject.put("workitemid", workitemid);
                                    jsonObject.put("workitemversionid", workitemversionid);
                                    jsonObject.put("workiteminstance", workiteminstance);
                                    jsonObject.put("usersequence", wids.getValue(i, "usersequence"));
                                    jsonObject.put("assigneddepartmentid", assigneddepartmentid);
                                    jsonArray.put(jsonObject);
                                    continue;
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        sb.append("</table>");
                        sb.append("</td>");
                        sb.append("</tr><tr><td>");
                        sb.append("<table cellpadding=2 cellspacing=0 border=0 style='width:100%'><tr>");
                        sb.append("<td>");
                        sb.append("<div name='editedFlagDiv' style='display:").append(editedflag && wids != null && wids.size() > 0 ? "block" : "none").append("'>");
                        sb.append("<span style='color:black;font-weight:bold;'><sup>#</sup></span> ").append(this.tp.translate("Edited")).append("</div>");
                        sb.append("</td>");
                        if (!this.viewOnly) {
                            sb.append("<td style='text-align:right;vertical-align:top;'>");
                            sb.append("<button onclick=\"cpEventSampleType.addNewService('").append(eventDefId).append("', '").append(cpEventSpecimenDefId).append("', '").append(sampletypeid).append("', '").append(row).append("', '").append(sampleTypeIndex).append("', '").append(workitemaddpage).append("');return false;\">");
                            sb.append(this.getTranslationProcessor().translate("Add")).append("</button>");
                            sb.append("</td>");
                        }
                        sb.append("</tr></table></td>");
                        sb.append("</tr></table>");
                        sb.append("<script type=\"text/javascript\">");
                        sb.append("var array_").append(eventDefId).append("_").append(sampleTypeIndex).append("_").append(row).append(" = ").append(jsonArray.toString()).append(";");
                        sb.append("</script>");
                    } else {
                        sb.append(this.renderColumn(column, sampleTypeIndex, row, filteredSpecimenDefs, stagPrefix, false, "setSpecimenDefSDIUpdate", sampletypeid));
                    }
                    sb.append("</td>");
                }
                sb.append("<td nowrap align=\"center\" style='vertical-align:top' width=\"30\" class=maintform_field>");
                if (this.viewOnly) {
                    sb.append("&nbsp;");
                } else {
                    sb.append("<img style=\"cursor: pointer;\" src=\"").append(IMG_SRC_DEL).append("\" title=\"Delete\" border=0 onclick=\"");
                    sb.append("cpEventSampleType.deleteSTSpecimenDef(").append(sampleTypeIndex).append(", ").append(row).append(");\">");
                }
                sb.append("</td>");
                sb.append("</tr>");
            }
        } else {
            sb.append("<tr id=\"").append(stagPrefix).append("norow\">");
            sb.append("<td colspan=").append(columnCount).append(" style='padding:6px' class='maintform_field'>");
            sb.append(this.tp.translate("No rows found"));
            sb.append("</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");
        if (!this.viewOnly) {
            sb.append("</td></tr><tr><td nowrap>");
            this.renderButton(sb, stagPrefix + "add", this.element.getPropertyListNotNull("displaytext").getProperty("specimenaddbuttontext", "Add To Plan"), "cpEventSampleType.addSTSpecimenDef(" + sampleTypeIndex + ");");
            sb.append("</td></tr><tr><td nowrap>");
        }
        if (!this.viewOnly) {
            sb.append("<table style=\"display:none\" id=\"").append(stagPrefix).append("templatetable\">");
            sb.append("<tr id=\"").append(stagPrefix).append("row_[row]\">");
            for (int col = 0; col < columns.size(); ++col) {
                String mode;
                PropertyList column = columns.getPropertyList(col);
                if (column == null || "hidden".equals(mode = column.getProperty("mode"))) continue;
                sb.append("<td nowrap align=\"").append(column.getProperty("align", "left")).append("\" class=maintform_field style='vertical-align:top'>");
                if ("workitemlookup".equals(mode)) {
                    sb.append("<table cellpadding=4 cellspacing=0 border=0 style='width:100%'><tr>");
                    sb.append("<td style='vertical-align:top'>");
                    sb.append("<table cellpadding=1 cellspacing=0 border=0 style='width:100%;' class='serviceManageTable'");
                    sb.append(" eventdefid=\"").append(eventDefId).append("\"");
                    sb.append(" specimendefid=\"NEW[row]\"");
                    sb.append(" sampletypeid=\"").append(sampletypeid).append("\"");
                    sb.append(" id='servicetable_").append(sampleTypeIndex).append("_[row]'>");
                    PropertyList workitemprops = column.getPropertyListNotNull("workitemprops");
                    PropertyList headertitle = workitemprops.getPropertyListNotNull("headertitle");
                    sb.append("<tr>");
                    sb.append("<td class='maintform_fieldtitle'>").append(headertitle.getProperty("Service", "Service")).append("</td>");
                    sb.append("<td class='maintform_fieldtitle'>").append(headertitle.getProperty("Apply", "Apply")).append("</td>");
                    sb.append("<td class='maintform_fieldtitle'>").append(headertitle.getProperty("Department", "Department")).append("</td>");
                    sb.append("<td class='maintform_fieldtitle'>&nbsp;</td>");
                    sb.append("</tr>");
                    sb.append("</table>");
                    sb.append("</td>");
                    sb.append("</tr><tr><td style='width:40px;vertical-align:top;padding:2px;text-align:right;'>");
                    sb.append("<button onclick=\"cpEventSampleType.addNewService('").append(eventDefId).append("', '', '").append(sampletypeid).append("', '[row]', '").append(sampleTypeIndex).append("');return false;\">");
                    sb.append(this.getTranslationProcessor().translate("Add")).append("</button>");
                    sb.append("</td>");
                    sb.append("</tr></table>");
                    sb.append("<script>cpEventSampleType.workitemdefaultapplyonadd = ").append("Y".equals(column.getPropertyListNotNull("workitemprops").getProperty("applyonaddflag", "N"))).append(";</script>");
                } else {
                    sb.append(this.renderColumn(column, sampleTypeIndex, 0, filteredSpecimenDefs, stagPrefix, true, "setSpecimenDefSDIUpdate", sampletypeid));
                }
                sb.append("</td>");
            }
            sb.append("<td nowrap align=\"center\" width=\"30\"class=maintform_field>");
            sb.append("<img style=\"cursor: pointer;\" src=\"").append(IMG_SRC_DEL).append("\" title=\"Delete\" border=0 onclick=\"");
            sb.append("cpEventSampleType.deleteSTSpecimenDef(").append(sampleTypeIndex).append(", [row]);\">");
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("</table>");
        }
        sb.append("</td></tr></table>");
        sb.append("<script>");
        sb.append("var serviceArray = ").append(jsonArray.toString()).append(";");
        sb.append("cpEventSampleType.viewOnlyMode = ").append(this.viewOnly).append(";");
        sb.append("cpEventSampleType.workitemviewpage = \"").append(workitemviewpage).append("\";");
        sb.append("cpEventSampleType.privateplanmaintpage = \"").append(privateplanmaintpage).append("\";");
        sb.append("</script>");
        return this.renderTab(stagPrefix + "tab", this.element.getPropertyListNotNull("displaytext").getProperty("specimentabtext", "Collection & Processing Plan") + " (" + filteredSpecimenDefs.size() + ")", sb);
    }

    private Tab renderAssayTypeData(StringBuilder html, StringBuilder jsonHtml, DataSet filteredAssayTypes, String cpEventSampleTypeId, int sampleTypeIndex) {
        String columnid;
        PropertyList column;
        int col;
        StringBuilder sb = new StringBuilder();
        String usedAssayTypeIds = null;
        DataSet arrivalOrderData = this.getArrivalOrders();
        String statPrefix = "cpeventsampletype_" + sampleTypeIndex + "_" + PREFIX_ASSAYTYPENAME + "_";
        sb.append("<table cellpadding=0 cellspacing=0 border=0 ><tr><td nowrap>");
        int columnCount = 5;
        sb.append("<table cellpadding=2 cellspacing=0 id=\"").append(statPrefix).append("table\" border=0 class=maintform_table><tbody>");
        sb.append("<tr class=maintform_tablehead>");
        PropertyListCollection columns = this.element.getCollectionNotNull("assaytypecolumns");
        for (col = 0; col < columns.size(); ++col) {
            column = columns.getPropertyList(col);
            if (column == null) continue;
            columnid = column.getProperty("column", "");
            sb.append("<td nowrap width=\"").append(column.getProperty("width", "120")).append("\" class=maintform_fieldtitle align='").append(column.getProperty("align", "left")).append("' style='padding:4px'><b>");
            sb.append(this.tp.translate(column.getProperty("title", columnid))).append("</b></td>");
            ++columnCount;
        }
        sb.append("<td nowrap width=\"30\" class=maintform_fieldtitle>&nbsp;</td>");
        sb.append("</tr>");
        if (filteredAssayTypes.getRowCount() > 0) {
            for (int row = 0; row < filteredAssayTypes.getRowCount(); ++row) {
                StringBuilder workItemHtml = new StringBuilder();
                StringBuilder specimenTypeHtml = new StringBuilder();
                StringBuilder arrivalOrderHtml = new StringBuilder();
                String assayTypeId = filteredAssayTypes.getValue(row, "s_assaytypeid");
                usedAssayTypeIds = usedAssayTypeIds == null ? assayTypeId : usedAssayTypeIds + ";" + assayTypeId;
                String mapId = filteredAssayTypes.getValue(row, "s_mapid");
                String specimenType = filteredAssayTypes.getValue(row, "specimentype");
                String arrivalOrder = filteredAssayTypes.getValue(row, "arrivalorder");
                String workItemId = filteredAssayTypes.getValue(row, "workitemId");
                String userSequence = filteredAssayTypes.getValue(row, "usersequence");
                if (row != 0) {
                    jsonHtml.append(",");
                }
                jsonHtml.append("{\"atrowindex\" : \"").append(row).append("\",");
                jsonHtml.append("\"s_mapid\" : \"").append(mapId).append("\",");
                jsonHtml.append("\"usersequence\" : \"").append(userSequence).append("\",");
                for (int col2 = 0; col2 < columns.size(); ++col2) {
                    String columnid2;
                    PropertyList column2 = columns.getPropertyList(col2);
                    if (column2 == null || (columnid2 = column2.getProperty("column", "").trim()).length() <= 0) continue;
                    int index = columnid2.lastIndexOf(" ");
                    if (index != -1) {
                        columnid2 = columnid2.substring(index).trim();
                    }
                    jsonHtml.append("\"").append(columnid2).append("\" : \"").append(filteredAssayTypes.getValue(row, columnid2, "")).append("\",");
                }
                jsonHtml.append("\"stateflag\" : \"U\"}");
                LinkedHashSet<String> vals = this.hsATWI.get(assayTypeId);
                if (this.viewOnly) {
                    workItemHtml.append("__d".equals(workItemId) ? this.tp.translate("use default") : workItemId);
                } else {
                    workItemHtml.append("<select id=\"").append(statPrefix).append("workitem_").append(row);
                    workItemHtml.append("\" onchange=\"cpEventSampleType.setAssayTypeSDIUpdate(").append(sampleTypeIndex);
                    workItemHtml.append(", ").append(row).append(", 'workitemid', this.value);\">");
                    workItemHtml.append("<option value=\"__d\">").append(this.tp.translate("use default")).append("</option>");
                    if (vals != null) {
                        for (String val : vals) {
                            workItemHtml.append("<option value=\"").append(val).append("\"");
                            if (val.equalsIgnoreCase(workItemId)) {
                                workItemHtml.append(" selected>").append(this.tp.translate(val)).append("</option>");
                                continue;
                            }
                            workItemHtml.append(">").append(this.tp.translate(val)).append("</option>");
                        }
                    }
                    workItemHtml.append("</select>");
                }
                if (this.viewOnly) {
                    specimenTypeHtml.append(specimenType);
                } else {
                    specimenTypeHtml.append("<select id=\"").append(statPrefix).append("specimentype_").append(row);
                    specimenTypeHtml.append("\" onchange=\"cpEventSampleType.setAssayTypeSDIUpdate(").append(sampleTypeIndex);
                    specimenTypeHtml.append(", ").append(row).append(", 'specimentype', this.value);\">");
                    specimenTypeHtml.append("<option value=\"\"></option>");
                    vals = this.hsSTCT.get(cpEventSampleTypeId);
                    if (vals != null) {
                        for (String containerType : vals) {
                            specimenTypeHtml.append("<option value=\"").append(containerType).append("\"");
                            if (containerType.equalsIgnoreCase(specimenType)) {
                                specimenTypeHtml.append(" selected>").append(this.tp.translate(containerType)).append("</option>");
                                continue;
                            }
                            specimenTypeHtml.append(">").append(this.tp.translate(containerType)).append("</option>");
                        }
                    }
                    specimenTypeHtml.append("</select>");
                }
                if (this.viewOnly) {
                    arrivalOrderHtml.append(arrivalOrder);
                } else {
                    arrivalOrderHtml.append("<select id=\"").append(statPrefix).append("arrivalorder_").append(row);
                    arrivalOrderHtml.append("\" onchange=\"cpEventSampleType.setAssayTypeSDIUpdate(").append(sampleTypeIndex);
                    arrivalOrderHtml.append(", ").append(row).append(", 'arrivalorder', this.value);\">");
                    arrivalOrderHtml.append("<option value=\"\"></option>");
                    if (arrivalOrderData.getRowCount() > 0) {
                        for (int di = 0; di < arrivalOrderData.getRowCount(); ++di) {
                            String arrivalOrderVal = arrivalOrderData.getValue(di, "refvalueid", "");
                            String arrivalOrderDispVal = arrivalOrderData.getValue(di, "refdisplayvalue", "");
                            arrivalOrderHtml.append("<option value=\"").append(arrivalOrderVal).append("\"");
                            if (arrivalOrderVal.equalsIgnoreCase(arrivalOrder)) {
                                arrivalOrderHtml.append(" selected>").append(this.tp.translate(arrivalOrderDispVal)).append("</option>");
                                continue;
                            }
                            arrivalOrderHtml.append(">").append(this.tp.translate(arrivalOrderDispVal)).append("</option>");
                        }
                    }
                    arrivalOrderHtml.append("</select>");
                }
                sb.append("<tr id=\"").append(statPrefix).append("row_").append(row).append("\">");
                for (int col3 = 0; col3 < columns.size(); ++col3) {
                    PropertyList column3 = columns.getPropertyList(col3);
                    if (column3 == null) continue;
                    sb.append("<td nowrap align=\"").append(column3.getProperty("align", "left")).append("\" class=maintform_field style='vertical-align:top'>");
                    String columnid3 = column3.getProperty("column");
                    String mode = column3.getProperty("mode");
                    if ("s_assaytypeid".equals(columnid3)) {
                        if ("default".equals(mode)) {
                            sb.append(assayTypeId);
                        } else {
                            sb.append("<span style='color:red'>").append(this.tp.translate("Only default mode supported")).append("</span>");
                        }
                    } else if ("workitemid".equals(columnid3)) {
                        if ("default".equals(mode)) {
                            sb.append(workItemHtml.toString());
                        } else {
                            sb.append("<span style='color:red'>").append(this.tp.translate("Only default mode supported")).append("</span>");
                        }
                    } else if ("arrivalorder".equals(columnid3)) {
                        if ("default".equals(mode)) {
                            sb.append(arrivalOrderHtml.toString());
                        } else {
                            sb.append("<span style='color:red'>").append(this.tp.translate("Only default mode supported")).append("</span>");
                        }
                    } else if ("specimentype".equals(columnid3)) {
                        if ("default".equals(mode)) {
                            sb.append(specimenTypeHtml.toString());
                        } else {
                            sb.append("<span style='color:red'>").append(this.tp.translate("Only default mode supported")).append("</span>");
                        }
                    } else if ("default".equals(mode)) {
                        sb.append("<span style='color:red'>").append(this.tp.translate("Column mode default not supported")).append("</span>");
                    } else {
                        sb.append(this.renderColumn(column3, sampleTypeIndex, row, filteredAssayTypes, statPrefix, false, "setAssayTypeSDIUpdate", cpEventSampleTypeId));
                    }
                    sb.append("</td>");
                }
                sb.append("<td nowrap align=\"center\" width=\"30\" class=\"maintform_field\">");
                if (this.viewOnly) {
                    sb.append("&nbsp;");
                } else {
                    sb.append("<img style=\"cursor: pointer;\" src=\"").append(IMG_SRC_DEL).append("\" title=\"Delete\" border=0 onclick=\"");
                    sb.append("cpEventSampleType.deleteSTAssayType(").append(sampleTypeIndex).append(", ").append(row).append(");\">");
                }
                sb.append("</td>");
                sb.append("</tr>");
            }
        } else {
            sb.append("<tr id=\"").append(statPrefix).append("norow\">");
            sb.append("<td colspan=").append(columnCount).append(" style='padding:6px' class='maintform_field'>");
            sb.append(this.tp.translate("No rows found"));
            sb.append("</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");
        if (!this.viewOnly) {
            sb.append("</td></tr><tr><td nowrap>");
            this.renderButton(sb, statPrefix + "add", this.element.getPropertyListNotNull("displaytext").getProperty("assaytypeaddbuttontext", "Add Assay Type"), "cpEventSampleType.addSTAssayType(" + sampleTypeIndex + ");");
            sb.append("</td></tr><tr><td nowrap>");
        }
        if (!this.viewOnly) {
            sb.append("<table style=\"display:none\" id=\"").append(statPrefix).append("templatetable\">");
            sb.append("<tr id=\"").append(statPrefix).append("row_[row]\" class=\"\">");
            for (col = 0; col < columns.size(); ++col) {
                column = columns.getPropertyList(col);
                if (column == null) continue;
                sb.append("<td nowrap align=\"").append(column.getProperty("align", "left")).append("\" class=maintform_field style='vertical-align:top'>");
                columnid = column.getProperty("column");
                String mode = column.getProperty("mode");
                if (mode.equals("default")) {
                    if ("s_assaytypeid".equals(columnid)) {
                        sb.append("<select id=\"").append(statPrefix).append("assaytype_[row]\" onchange=\"cpEventSampleType.validateAssayType(");
                        sb.append(sampleTypeIndex).append(", [row], this.value);\">");
                        sb.append("</select>");
                    } else if ("workitemid".equals(columnid)) {
                        sb.append("<select id=\"").append(statPrefix).append("workitem_[row]\" onchange=\"cpEventSampleType.setAssayTypeSDIUpdate(");
                        sb.append(sampleTypeIndex).append(", [row], 'workitemid', this.value);\">");
                        sb.append("<option value=\"__d\">").append(this.tp.translate("use default")).append("</option>");
                        sb.append("</select>");
                    } else if ("arrivalorder".equals(columnid)) {
                        sb.append("<select id=\"").append(statPrefix).append("arrivalorder_[row]\" onchange=\"cpEventSampleType.setAssayTypeSDIUpdate(");
                        sb.append(sampleTypeIndex).append(", [row], 'arrivalorder', this.value);\">");
                        sb.append("<option value=\"\"></option>");
                        if (arrivalOrderData.getRowCount() > 0) {
                            for (int di = 0; di < arrivalOrderData.getRowCount(); ++di) {
                                String arrivalOrderVal = arrivalOrderData.getValue(di, "refvalueid", "");
                                String arrivalOrderDispVal = arrivalOrderData.getValue(di, "refdisplayvalue", "");
                                sb.append("<option value=\"").append(arrivalOrderVal).append("\">").append(this.tp.translate(arrivalOrderDispVal)).append("</option>");
                            }
                        }
                        sb.append("</select>");
                    } else if ("specimentype".equals(columnid)) {
                        sb.append("<select id=\"").append(statPrefix).append("specimentype_[row]\" onchange=\"cpEventSampleType.setAssayTypeSDIUpdate(");
                        sb.append(sampleTypeIndex).append(", [row], 'specimentype', this.value);\">");
                        sb.append("<option value=\"\"></option>");
                        sb.append("</select>");
                    } else {
                        sb.append("<span style='color:red'>").append(this.tp.translate("Default mode not supported")).append("</span>");
                    }
                } else {
                    sb.append(this.renderColumn(column, sampleTypeIndex, 0, filteredAssayTypes, statPrefix, true, "setAssayTypeSDIUpdate", cpEventSampleTypeId));
                }
                sb.append("</td>");
            }
            sb.append("<td nowrap align=\"center\" width=\"30\" class=\"maintform_field\">");
            sb.append("<img style=\"cursor: pointer;\" src=\"").append(IMG_SRC_DEL).append("\" title=\"Delete\" border=0");
            sb.append(" onclick=\"cpEventSampleType.deleteSTAssayType(").append(sampleTypeIndex).append(", [row]);\">");
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("</table>");
        }
        sb.append("</td></tr></table>");
        if (usedAssayTypeIds != null) {
            html.append("<script language=\"JavaScript\">");
            html.append("saSampleTypeAssayTypeUsed['").append(cpEventSampleTypeId).append("'] = '").append(usedAssayTypeIds).append("';");
            html.append("</script>");
        }
        return this.renderTab(statPrefix + "tab", this.element.getPropertyListNotNull("displaytext").getProperty("assaytypetabtext", "Assay Types") + " (" + filteredAssayTypes.size() + ")", sb);
    }

    private void renderButton(StringBuilder html, String id, String text, String js) {
        String img = IMG_SRC_ADD;
        String tip = "Add New Row";
        String appearance = "standard";
        String margin = "";
        String style = "";
        String width = "";
        html.append("<table cellspacing=\"0\" cellpadding=\"1\" border=\"0\" align=right><tr>");
        html.append("<td nowrap>");
        Button button = new Button(this.pageContext);
        button.setId(id);
        button.setText(text);
        button.setImg(img);
        button.setTip(tip);
        button.setAppearance(appearance);
        button.setMargin(margin);
        button.setStyle(style);
        button.setWidth(width);
        button.setAction(js);
        html.append(button.getHtml());
        html.append("</td>");
        html.append("</tr></table>");
    }

    private Tab renderTab(String id, String text, StringBuilder mainHtml) {
        Tab tab = new Tab();
        tab.setId(id);
        tab.setText(text);
        tab.setBodywidth("100%");
        tab.setWidth("80");
        tab.setExpandable("true");
        tab.setExpanded("true");
        tab.setHighlight("true");
        tab.setContent(mainHtml.toString());
        tab.setCollapsedtext(this.getTranslationProcessor().translate("Click the tab to show more information") + ".");
        return tab;
    }

    private boolean checkLockState(String datasetName) {
        boolean theReturn;
        try {
            DataSet data = this.sdiInfo.getDataSet(datasetName);
            String lockedBy = data.getValue(0, "__lockedby", "");
            if (lockedBy == null || lockedBy.length() == 0) {
                theReturn = false;
                this.logger.debug("Not locked.");
            } else {
                theReturn = true;
                this.logger.debug("Locked by " + lockedBy + ".");
            }
        }
        catch (Exception e) {
            theReturn = true;
            this.logger.warn("Could not obtain lock information therefore default to locked.");
        }
        return theReturn;
    }

    private String renderColumn(PropertyList column, int sampleTypeIndex, int row, DataSet ds, String stagPrefix, boolean template, String onchangefunction, String sampletypeid) {
        String onchange;
        String fieldid;
        boolean hasValidations;
        String columnid = column.getProperty("column");
        String value = "";
        String width = column.getProperty("width", "80");
        width = width.endsWith("%") ? width : (!width.endsWith("px") ? width + "px" : width);
        String columnmode = column.getProperty("mode", "input");
        String validation = column.getProperty("validation", "").trim();
        PropertyList link = column.getPropertyListNotNull("link");
        String link_href = link.getProperty("href").trim();
        String link_target = link.getProperty("target");
        String link_tip = link.getProperty("tip");
        String pseudo = column.getProperty("pseudo").trim();
        String displayvalue = column.getProperty("displayvalue");
        String linkreftypeid = column.getProperty("linkreftypeid");
        String translatevalue = column.getProperty("translatevalue", "N");
        PropertyList dropdowndefinition = column.getPropertyListNotNull("dropdowndefinition");
        boolean mandatory = validation.toLowerCase().contains("mandatory");
        boolean disableonsave = "Y".equals(column.getProperty("disableonsave"));
        boolean bl = hasValidations = validation.trim().length() > 0;
        if (disableonsave && !template) {
            columnmode = "readonly";
        }
        if (template) {
            value = column.getProperty("default", "");
        } else if (StringUtil.getLen(columnid) > 0L) {
            int index = columnid.lastIndexOf(" ");
            if (index != -1) {
                columnid = columnid.substring(index).trim();
            }
            value = ds.getValue(row, columnid, "");
        }
        if (!template && displayvalue.length() > 0) {
            if (columnmode.equals("checkbox")) {
                if (this.viewOnly) {
                    value = this.parseDisplayvalue(value.trim(), displayvalue.trim());
                }
            } else {
                value = this.parseDisplayvalue(value.trim(), displayvalue.trim());
            }
        }
        if (value.contains("[")) {
            value = this.parseExpression(value, ds, row);
        }
        if (pseudo.length() > 0) {
            if (template) {
                return "&nbsp;";
            }
            value = this.parseExpression(pseudo, ds, row);
            if (link_href.length() > 0) {
                link_href = this.parseExpression(link_href, ds, row);
                try {
                    link_href = URLEncoder.encode(link_href, "UTF-8");
                }
                catch (UnsupportedEncodingException index) {
                    // empty catch block
                }
                return "<a href='" + link_href + "' target='" + link_target + "' title='" + link_tip + "'>" + value + "</a>";
            }
            return value;
        }
        if (this.viewOnly) {
            if (value.length() == 0) {
                value = "&nbsp;";
            } else {
                if (columnmode.equals("lookupversioned")) {
                    String versioncolumnid = column.getProperty("versioncolumnid", "").trim();
                    if (versioncolumnid.length() == 0) {
                        return "<span style='color:red'>Missing version column id</span>";
                    }
                    return value + " (" + this.tp.translate("version") + ": " + ds.getValue(row, versioncolumnid, "") + ")";
                }
                if ("Y".equals(translatevalue)) {
                    value = this.getTranslationProcessor().translate(value);
                }
            }
            if (link_href.length() > 0) {
                link_href = this.parseExpression(link_href, ds, row);
                try {
                    link_href = URLEncoder.encode(link_href, "UTF-8");
                }
                catch (UnsupportedEncodingException versioncolumnid) {
                    // empty catch block
                }
                return "<a href='" + link_href + "' target='" + link_target + "' title='" + link_tip + "'>" + value + "</a>";
            }
            return value;
        }
        if (template) {
            fieldid = stagPrefix + columnid + "_[row]";
            onchange = "cpEventSampleType." + onchangefunction + "( " + sampleTypeIndex + ", [row], '" + columnid + "', this.value )";
        } else {
            fieldid = stagPrefix + columnid + "_" + row;
            onchange = "cpEventSampleType." + onchangefunction + "( " + sampleTypeIndex + "," + row + ", '" + columnid + "', this.value )";
        }
        if (columnmode.equals("checkbox")) {
            onchange = template ? "cpEventSampleType." + onchangefunction + "( " + sampleTypeIndex + ", [row], '" + columnid + "', this.checked ? 'Y' : 'N' )" : "cpEventSampleType." + onchangefunction + "( " + sampleTypeIndex + "," + row + ", '" + columnid + "', this.checked ? 'Y' : 'N' )";
            return "<input id='" + fieldid + "' type=checkbox onchange=\"" + onchange + "\"" + ("Y".equals(value) ? " checked" : "") + (hasValidations ? " class=\"" + (mandatory ? "mandatoryfield " : "") + "\" validation=\"" + validation + "\"" : "") + ">";
        }
        if (columnmode.equals("lookup")) {
            String lookuppage = column.getProperty("lookuppage", "");
            if (lookuppage.contains("[") && (lookuppage = StringUtil.replaceAll(lookuppage, "[s_sampletypeid]", sampletypeid)).contains("[")) {
                lookuppage = this.parseExpression(lookuppage, ds, row);
            }
            return this.getLookupHtml(fieldid, value, onchange, lookuppage, mandatory, width, validation);
        }
        if (columnmode.equals("lookupversioned")) {
            String versioncolumnid = column.getProperty("versioncolumnid", "").trim();
            if (versioncolumnid.length() == 0) {
                return "<span style='color:red'>Missing version column id</span>";
            }
            String lookuppage = column.getProperty("lookuppage", "");
            if (lookuppage.contains("[") && (lookuppage = StringUtil.replaceAll(lookuppage, "[s_sampletypeid]", sampletypeid)).contains("[")) {
                lookuppage = this.parseExpression(lookuppage, ds, row);
            }
            return this.getLookupVersionHtml(fieldid, value, template ? "" : ds.getValue(row, versioncolumnid, ""), onchange, lookuppage, mandatory, width, columnid, versioncolumnid, validation);
        }
        if (columnmode.equals("dropdownlist")) {
            String dropdownsql;
            PropertyList ddprops = new PropertyList(dropdowndefinition);
            String querywhere = ddprops.getProperty("querywhere", "");
            if (querywhere.contains("[")) {
                if ((querywhere = StringUtil.replaceAll(querywhere, "[s_sampletypeid]", sampletypeid)).contains("[")) {
                    querywhere = this.parseExpression(querywhere, ds, row);
                }
                ddprops.setProperty("querywhere", querywhere);
            }
            if ((dropdownsql = column.getProperty("dropdownsql", "")).contains("[") && (dropdownsql = StringUtil.replaceAll(dropdownsql, "[s_sampletypeid]", sampletypeid)).contains("[")) {
                dropdownsql = this.parseExpression(dropdownsql, ds, row);
            }
            return this.getDropDownHtml(fieldid, value, onchange, mandatory, linkreftypeid, dropdownsql, ddprops, validation);
        }
        if (link_href.length() > 0) {
            if (StringUtil.getLen(value) > 0L) {
                if (link_href.contains("[") && (link_href = StringUtil.replaceAll(link_href, "[s_sampletypeid]", sampletypeid)).contains("[")) {
                    link_href = this.parseExpression(link_href, ds, row);
                }
                try {
                    link_href = URLEncoder.encode(link_href, "UTF-8");
                }
                catch (UnsupportedEncodingException ddprops) {
                    // empty catch block
                }
                return "<a href='" + link_href + "' target='" + link_target + "' title='" + link_tip + "'>" + value + "</a>";
            }
            return "&nbsp;";
        }
        if (columnmode.equals("input")) {
            return "<input id='" + fieldid + "' onchange=\"" + onchange + "\" " + (hasValidations ? " class=\"" + (mandatory ? "mandatoryfield " : "") + "\" validation=\"" + validation + "\"" : "") + " value='" + value + "' style='width:" + width + "'>";
        }
        if (columnmode.equals("longinput")) {
            return "<textarea rows=3 id='" + fieldid + "' onchange=\"" + onchange + "\" " + (hasValidations ? " class=\"" + (mandatory ? "mandatoryfield " : "") + "\" validation=\"" + validation + "\"" : "") + " style='width:" + width + ";height:100%;'>" + value + "</textarea>";
        }
        if (columnmode.equals("textarea")) {
            return "<textarea id='" + fieldid + "' onchange=\"" + onchange + "\" " + (hasValidations ? " class=\"" + (mandatory ? "mandatoryfield " : "") + "\" validation=\"" + validation + "\"" : "") + " style='width:" + width + ";height:100%;' rows=3>" + value + "</textarea>";
        }
        if (columnmode.equals("datelookup")) {
            String dateFormat = column.getProperty("format", "");
            if (dateFormat.length() > 0) {
                ds.setDateDisplayFormat(columnid, ElementUtil.getDateFormat(this.pageContext, dateFormat, false));
                value = ds.getValue(row, columnid);
            }
            return this.getDatelookupHtml(fieldid, value, onchange, mandatory, width, dateFormat, validation);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<div id='").append(fieldid).append("'>");
        if (StringUtil.getLen(value) > 0L) {
            if ("Y".equals(translatevalue)) {
                value = this.getTranslationProcessor().translate(value);
            }
            sb.append(value);
        } else {
            sb.append("&nbsp;");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private String parseDisplayvalue(String value, String displayvalue) {
        if (displayvalue.trim().length() > 0) {
            String[] st;
            for (String str : st = StringUtil.split(displayvalue, ";")) {
                int equalindex = str.indexOf(61);
                String replacestr = str.substring(0, equalindex).trim();
                String replacement = str.substring(++equalindex).trim();
                if (!value.equals(replacestr)) continue;
                value = value.replace(replacestr, replacement);
                break;
            }
        }
        return value;
    }

    private String parseExpression(String expression, DataSet data, int row) {
        String[] tokens;
        for (String token : tokens = StringUtil.getTokens(expression, "[", "]")) {
            if ("rownum".equals(token)) {
                expression = StringUtil.replaceAll(expression, "[" + token + "]", String.valueOf(row));
                continue;
            }
            if ("elementid".equals(token)) {
                expression = StringUtil.replaceAll(expression, "[" + token + "]", this.elementid);
                continue;
            }
            if (token.contains("=")) {
                if (token.startsWith("columnid=")) {
                    String column = token.substring(9);
                    expression = StringUtil.replaceAll(expression, "[" + token + "]", data.getValue(row, column));
                    continue;
                }
                if (!token.startsWith("param=")) continue;
                String string = token.substring(6);
                continue;
            }
            if (!data.isValidColumn(token)) continue;
            expression = StringUtil.replaceAll(expression, "[" + token + "]", data.getValue(row, token));
        }
        return expression;
    }

    private String getLookupHtml(String fieldid, String value, String onchange, String lookupPage, boolean mandatory, String width, String validation) {
        if (StringUtil.getLen(lookupPage) == 0L) {
            return "<font color=red>Lookup URL not defined</font>";
        }
        String s = "<table cellpadding=0 cellspacing=0 border=0>";
        s = s + "<tr><td>";
        s = s + "<input id=\"" + fieldid + "\" readonly";
        s = s + (validation.length() > 0 ? " class=\"" + (mandatory ? "mandatoryfield " : "") + "\" validation=\"" + validation + "\"" : "");
        s = s + " onchange=\"" + onchange + "\"";
        s = s + " value=\"" + value + "\"";
        s = s + " style='width:" + width + "'>";
        s = s + "</td><td valign=middle>";
        s = s + "<img id='img_" + fieldid + "' src='WEB-CORE/elements/images/lookup.gif' style='cursor:pointer'";
        if (lookupPage.startsWith("javascript:")) {
            lookupPage = StringUtil.replaceAll(lookupPage, "[fieldid]", fieldid);
            s = s + " onclick=\"" + lookupPage + "\"";
        } else {
            s = s + " onclick=\"cpEventSampleType.lookupPageSelect('" + lookupPage + "', '" + fieldid + "')\"";
        }
        s = s + ">";
        s = s + "</td></tr>";
        s = s + "</table>";
        return s;
    }

    private String getLookupVersionHtml(String fieldid, String value, String versionvalue, String onchange, String lookupPage, boolean mandatory, String width, String columnid, String versioncolumnid, String validation) {
        if (StringUtil.getLen(lookupPage) == 0L) {
            return "<font color=red>Lookup URL not defined</font>";
        }
        String s = "<table cellpadding=0 cellspacing=0 border=0 id='" + fieldid + "_table'>";
        s = s + "<tr><td>";
        s = s + "<input id=\"" + fieldid + "\" readonly";
        s = s + (validation.length() > 0 ? " class=\"" + (mandatory ? "mandatoryfield " : "") + "\" validation=\"" + validation + "\"" : "");
        s = s + " onchange=\"" + onchange + "\"";
        s = s + " value=\"" + value + "\"";
        s = s + " style='width:" + width + "'>";
        s = s + "</td>";
        s = s + "<td>";
        s = s + "<input id=\"" + StringUtil.replaceAll(fieldid, columnid, versioncolumnid) + "\" readonly";
        s = s + (mandatory ? " class=mandatoryfield" : "");
        s = s + " onchange=\"" + StringUtil.replaceAll(onchange, columnid, versioncolumnid) + "\"";
        s = s + " value=\"" + versionvalue + "\"";
        s = s + " style='width:30px'>";
        s = s + "</td>";
        s = s + "<td valign=middle>";
        s = s + "<img id='img_" + fieldid + "' src='WEB-CORE/elements/images/lookup.gif' style='cursor:pointer'";
        if (lookupPage.startsWith("javascript:")) {
            lookupPage = StringUtil.replaceAll(lookupPage, "[fieldid]", fieldid);
            s = s + " onclick=\"" + lookupPage + "\"";
        } else {
            s = s + " onclick=\"cpEventSampleType.lookupVersioned('" + lookupPage + "', '" + fieldid + "', '" + StringUtil.replaceAll(fieldid, columnid, versioncolumnid) + "')\"";
        }
        s = s + ">";
        s = s + "</td></tr>";
        s = s + "</table>";
        s = s + "<div style='display:none' id='" + fieldid + "_msgdiv'><span style='color:gray'>" + this.getTranslationProcessor().translate("Duplicate Specimen Type") + "</span></div>";
        return s;
    }

    private String getDropDownHtml(String fieldid, String value, String onchange, boolean mandatory, String reftypeid, String dropdownsql, PropertyList dropdowndefinition, String validation) {
        StringBuilder sb = new StringBuilder();
        sb.append("<select id='").append(fieldid).append("' onchange=\"").append(onchange).append("\"");
        sb.append(validation.length() > 0 ? " class=\"" + (mandatory ? "mandatoryfield " : "") + "\" validation=\"" + validation + "\"" : "");
        sb.append(">");
        sb.append(this.getDropDownOptionsHTML(reftypeid, dropdownsql, dropdowndefinition, value));
        sb.append("</select>");
        return sb.toString();
    }

    private String getDropDownOptionsHTML(String reftypeid, String dropdownsql, PropertyList dropdowndefinition, String value) {
        DataSet ds = null;
        String valuecolumn = "";
        String displaycolumn = "";
        if (StringUtil.getLen(reftypeid) > 0L) {
            ds = this.getQueryProcessor().getRefTypeDataSet(reftypeid);
            if (ds != null && ds.size() > 0) {
                valuecolumn = "refvalueid";
                displaycolumn = "refdisplayvalue";
            }
        } else if (StringUtil.getLen(dropdownsql) > 0L) {
            ds = this.getQueryProcessor().getSqlDataSet(dropdownsql);
            if (ds != null && ds.size() > 0) {
                valuecolumn = ds.getColumnId(0);
                displaycolumn = ds.getColumnCount() > 1 ? ds.getColumnId(1) : valuecolumn;
            }
        } else {
            String sdcid = dropdowndefinition.getProperty("sdcid");
            String queryfrom = dropdowndefinition.getProperty("queryfrom");
            String querywhere = dropdowndefinition.getProperty("querywhere");
            String queryorderby = dropdowndefinition.getProperty("queryorderby");
            valuecolumn = dropdowndefinition.getProperty("valuecolumn");
            displaycolumn = dropdowndefinition.getProperty("displaycolumn", valuecolumn);
            if (StringUtil.getLen(sdcid) > 0L) {
                SDIData sdiData;
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setRequestItem("primary[" + valuecolumn + (displaycolumn.equals(valuecolumn) ? "" : "," + displaycolumn) + "]");
                sdiRequest.setSDCid(sdcid);
                if (queryfrom.length() > 0) {
                    sdiRequest.setQueryFrom(queryfrom);
                }
                if (querywhere.length() > 0) {
                    sdiRequest.setQueryWhere(querywhere);
                }
                if (queryorderby.length() > 0) {
                    sdiRequest.setQueryOrderBy(queryorderby);
                }
                if ((sdiData = this.getSDIProcessor().getSDIData(sdiRequest)) != null) {
                    ds = sdiData.getDataset(PARENTDATASET);
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<option value=''></option>");
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String ddvalue = ds.getValue(i, valuecolumn);
                sb.append("<option value='").append(ddvalue).append("'");
                sb.append(value.equals(ddvalue) ? " selected" : "");
                sb.append(">").append(ds.getValue(i, displaycolumn, ddvalue)).append("</option>");
            }
        }
        return sb.toString();
    }

    private String getDatelookupHtml(String fieldid, String value, String onchange, boolean mandatory, String width, String columndateformat, String validation) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table cellpadding=0 cellspacing=0 border=0>");
        sb.append("<tr datatype='skip'><td>");
        sb.append("<input id='").append(fieldid).append("'");
        sb.append(validation.length() > 0 ? " class=\"" + (mandatory ? "mandatoryfield " : "") + "\" validation=\"" + validation + "\"" : "");
        sb.append(" value='").append(value).append("'");
        sb.append(" style='width:").append(width).append("' datatype='D'");
        sb.append(" onchange=\"").append(onchange).append("\"");
        sb.append("></td><td valign=middle>");
        sb.append("<img src='WEB-CORE/elements/images/lookup_date.gif' id='img_").append(fieldid).append("' style='cursor:pointer'");
        sb.append(" onclick='sapphire.lookup.date.open( \"").append(fieldid).append("\", \"\", \"\", \"\", \"\", \"").append(columndateformat).append("\" );return false;'>");
        sb.append("</td></tr></table>");
        return sb.toString();
    }
}

