/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.childsampleplan;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.pageelements.controls.Button;
import java.util.HashMap;
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class BaseChildSamplePlan
extends BaseElement {
    protected boolean isEmbedded = false;

    public String getElementHtml(String childsampleplanid, String childsampleplanversionid) {
        TranslationProcessor translationProcessor = this.getTranslationProcessor();
        StringBuilder sb = new StringBuilder();
        PropertyList testselection = this.element.getPropertyListNotNull("testselection");
        boolean allowtemplateselection = "Y".equals(this.element.getPropertyListNotNull("template").getProperty("show", "Y"));
        boolean allowtestselection = "Y".equals(testselection.getProperty("allow", "N"));
        boolean allowcontainertypeselection = "Y".equals(this.element.getProperty("allowcontainertypeselection", "N"));
        boolean typeIsBioBank = "BioBanking".equals(this.element.getProperty("type", "BioBanking"));
        String mode = this.element.getProperty("mode", "Edit");
        boolean viewOnlyMode = "View".equals(mode);
        boolean protectedMode = "Protected".equals(mode);
        boolean workitemapplyonadd = "Y".equals(testselection.getProperty("applyonaddflag", "N"));
        String workitemviewpage = testselection.getProperty("viewpage", "rc?command=page&page=WorkItemView");
        String workitemaddpage = testselection.getProperty("addpage", "rc?command=page&page=ServiceLookupMulti");
        String workitemdisplayformat = testselection.getProperty("displayformat", "[workitemid] (v[workitemversionid])");
        String elementid = this.element.getId();
        PropertyListCollection columns = this.element.getCollectionNotNull("columns");
        SafeSQL safeSQL = new SafeSQL();
        boolean isOracle = this.getConnectionProcessor().isOra();
        if (StringUtil.getLen(childsampleplanid) == 0L) {
            sb.append(this.renderError("Child Sample Plan not found in request"));
        } else {
            String buttonplacement = this.element.getProperty("buttonplacement", "Bottom");
            PropertyList columntitletext = this.element.getPropertyListNotNull("columntitletext");
            StringBuilder sql = new StringBuilder();
            sql.append("select sampletypeid, versionstatus, embeddedflag");
            sql.append(" from s_childsampleplan");
            sql.append(" where s_childsampleplanid = ").append(safeSQL.addVar(childsampleplanid)).append(" and s_childsampleplanversionid = ").append(safeSQL.addVar(childsampleplanversionid));
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                String[] displaytokens;
                DataSet WICompletedStatusDS;
                DataSet processTypeDS;
                int i;
                DataSet dsUnits;
                this.isEmbedded = "Y".equals(ds.getString(0, "embeddedflag", "N"));
                String parentsampletype = ds.getString(0, "sampletypeid", "");
                String versionstatus = ds.getValue(0, "versionstatus", "P");
                if (!viewOnlyMode) {
                    viewOnlyMode = !"P".equals(versionstatus);
                }
                StringBuilder buttonHtml = new StringBuilder();
                if (!viewOnlyMode && !protectedMode) {
                    Button button = new Button(this.pageContext);
                    button.setImg("WEB-CORE/images/png/MoveUp.png");
                    button.setAction("childsampleplan.moveUp()");
                    button.setStyle("childsampleplan_button");
                    buttonHtml.append(button.getHtml());
                    button.setImg("WEB-CORE/images/png/MoveDown.png");
                    button.setAction("childsampleplan.moveDown()");
                    buttonHtml.append(button.getHtml());
                    button.setText("Add Child");
                    button.setImg("WEB-CORE/images/png/Add.png");
                    button.setAction("childsampleplan.addChild()");
                    buttonHtml.append(button.getHtml());
                    if (typeIsBioBank) {
                        button.setText("Add Grand Child");
                        button.setImg("WEB-CORE/images/png/Add.png");
                        button.setAction("childsampleplan.addGrandChild()");
                        buttonHtml.append(button.getHtml());
                    }
                    button.setText("Remove");
                    button.setImg("WEB-CORE/images/png/Delete.png");
                    button.setAction("childsampleplan.removeRow()");
                    buttonHtml.append(button.getHtml());
                }
                sb.append("<style type='text/css'>");
                sb.append("\nspan.applyonadd{border:1px solid #999;background:#339900;padding: 1px 4px;;margin:1px;color:#fff;border-radius: 5px;font-size:10px;cursor:pointer;}");
                sb.append("\nspan.onlyadd{border:1px solid #999;background:#eee;padding: 1px 4px;margin:1px;border-radius: 5px;font-size: 10px;cursor:pointer;}");
                sb.append("\ntd.oicon {padding:0 1px;}");
                sb.append("\n.sort-highlight{background: #cfeff7; height: 24px;}");
                sb.append("\n.sort-highlight > td{border-top:1px solid #5c97bf;border-bottom:1px solid #5c97bf;}");
                sb.append("\n.sort-highlight > td:first-child{border-left:1px solid #5c97bf;}");
                sb.append("\n.sort-highlight > td:last-child{border-right:1px solid #5c97bf;}");
                sb.append("\n.typeaheadfield {border:1px solid #4daf7c;}");
                sb.append("\n.childplan_gc { background:#f5f5f5 }");
                sb.append("\n.childplan_button { background:#f5f5f5 }");
                sb.append("\n</style>");
                sb.append("\n<input type='hidden' name='__propertyhandler_").append(elementid).append("' value='com.labvantage.opal.elements.childsampleplan.ChildSamplePlanPropertyHandler'/>");
                sb.append("\n<input type='hidden' name='__").append(elementid).append("_data' id='__").append(elementid).append("_data' value=''>");
                sb.append("\n<input type='hidden' name='__").append(elementid).append("_sdcid' id='__").append(elementid).append("_sdcid' value='ChildSamplePlan'>");
                sb.append("\n<input type='hidden' name='__").append(elementid).append("_keyid1' id='__").append(elementid).append("_keyid1' value='").append(childsampleplanid).append("'>");
                sb.append("\n<input type='hidden' name='__").append(elementid).append("_keyid2' id='__").append(elementid).append("_keyid2' value='").append(childsampleplanversionid).append("'>");
                sb.append("\n<input type='hidden' name='__servicearray_").append(elementid).append("' id='__servicearray_").append(elementid).append("' value=''>");
                sb.append("\n<script type='text/javascript' src='WEB-OPAL/elements/childsampleplan/scripts/childsampleplan.js'></script>");
                if (!(viewOnlyMode || "Bottom".equals(buttonplacement) || protectedMode)) {
                    sb.append((CharSequence)buttonHtml);
                    sb.append("<div style='height:10px'></div>");
                }
                sb.append("<table class='maintform_table' cellpadding='4px' style='margin-left:4px;' id='").append(elementid).append("_table'>");
                sb.append("<tr>");
                sb.append("<th class='maintform_fieldtitle' style='width:30px'>");
                sb.append("<input type='checkbox' id='").append(elementid).append("_selectall' ").append(viewOnlyMode ? "disabled" : "onclick='childsampleplan.handleSelectAllClick( this )'").append("></th>");
                if (typeIsBioBank) {
                    sb.append("<th class='maintform_fieldtitle'>").append(translationProcessor.translate(columntitletext.getProperty("Child Type", "Child Type"))).append("</th>");
                }
                if (allowtemplateselection) {
                    sb.append("<th class='maintform_fieldtitle'>").append(translationProcessor.translate(columntitletext.getProperty("Template", "Template"))).append("</th>");
                }
                sb.append("<th class='maintform_fieldtitle'>").append(translationProcessor.translate(columntitletext.getProperty("Count", "Count"))).append("</th>");
                sb.append("<th class='maintform_fieldtitle'>").append(translationProcessor.translate(columntitletext.getProperty("Quantity", "Quantity"))).append("</th>");
                if (typeIsBioBank) {
                    sb.append("<th class='maintform_fieldtitle'>").append(translationProcessor.translate(columntitletext.getProperty("Derivative Options", "Derivative Options"))).append("</th>");
                }
                if (allowcontainertypeselection) {
                    sb.append("<th class='maintform_fieldtitle'>").append(translationProcessor.translate(columntitletext.getProperty("Container Type", "Container Type"))).append("</th>");
                }
                sb.append("<th class='maintform_fieldtitle'>").append(translationProcessor.translate(columntitletext.getProperty("Label Method", "Label Method"))).append("</th>");
                if (typeIsBioBank) {
                    sb.append("<th class='maintform_fieldtitle'>").append(translationProcessor.translate(columntitletext.getProperty("Process", "Process"))).append("</th>");
                }
                if (allowtestselection) {
                    sb.append("<th class='maintform_fieldtitle'>").append(translationProcessor.translate(columntitletext.getProperty("Test", "Services"))).append("</th>");
                }
                sb.append("<th class='maintform_fieldtitle'>").append(translationProcessor.translate(columntitletext.getProperty("Process Instructions", "Process Instructions"))).append("</th>");
                if (this.isEmbedded) {
                    sb.append("<th class='maintform_fieldtitle'>").append(translationProcessor.translate(columntitletext.getProperty("Completed When", "Completed When"))).append("</th>");
                }
                for (int col = 0; col < columns.size(); ++col) {
                    PropertyList columnProps = columns.getPropertyList(col);
                    String column = columnProps.getProperty("column").trim();
                    if (column.length() <= 0 || "hidden".equals(columnProps.getProperty("mode"))) continue;
                    sb.append("<th class='maintform_fieldtitle'>").append(columnProps.getProperty("title")).append("</th>");
                }
                sb.append("</tr>");
                sb.append("<tr>");
                sb.append("<td colspan='10' id='").append(elementid).append("_norecords' class='maintform_field'>").append(translationProcessor.translate("No records found")).append("</td>");
                sb.append("<tr>");
                sb.append("</table>");
                if (!(viewOnlyMode || "Top".equals(buttonplacement) || protectedMode)) {
                    sb.append("<div style='height:10px'></div>");
                    sb.append((CharSequence)buttonHtml);
                }
                sb.append("\n<script type='text/javascript'>");
                sb.append("\nchildsampleplan.typeisbiobank = ").append(typeIsBioBank).append(";");
                sb.append("\nchildsampleplan.parentSampleType = \"").append(parentsampletype).append("\";");
                sb.append("\nchildsampleplan.elementid = \"").append(elementid).append("\";");
                sb.append("\nchildsampleplan.allowTestSelection = ").append(allowtestselection).append(";");
                sb.append("\nchildsampleplan.viewOnlyMode = ").append(viewOnlyMode).append(";");
                sb.append("\nchildsampleplan.protectedMode = ").append(protectedMode).append(";");
                sb.append("\nchildsampleplan.workitemviewpage = \"").append(workitemviewpage).append("\";");
                sb.append("\nchildsampleplan.workitemaddpage = \"").append(workitemaddpage).append("\";");
                sb.append("\nchildsampleplan.childsampleplanid = \"").append(childsampleplanid).append("\";");
                sb.append("\nchildsampleplan.childsampleplanversionid = \"").append(childsampleplanversionid).append("\";");
                sb.append("\nchildsampleplan.allowcontainertypeselection = ").append(allowcontainertypeselection).append(";");
                sb.append("\nchildsampleplan.isembedded = ").append(this.isEmbedded).append(";");
                sb.append("\nchildsampleplan.workitemdefaultapplyonadd = ").append(workitemapplyonadd).append(";");
                sb.append("\nchildsampleplan.privateplanmaintpage = \"").append(testselection.getProperty("privateplanmaintpage", "rc?command=page&page=LV_ProtectedSamplePlanMaint")).append("\";");
                sb.append("\nchildsampleplan.departmentrestrictivewhere = \"").append(testselection.getProperty("departmentrestrictivewhere", "")).append("\";");
                sb.append("\nchildsampleplan.additionalcolumns = ").append(columns.toJSONArray());
                PropertyList headertitle = testselection.getPropertyListNotNull("headertitle");
                sb.append("\nchildsampleplan.headertext_Service = \"").append(headertitle.getProperty("Service", "Service")).append("\";");
                sb.append("\nchildsampleplan.headertext_Apply = \"").append(headertitle.getProperty("Apply", "Apply")).append("\";");
                sb.append("\nchildsampleplan.headertext_Department = \"").append(headertitle.getProperty("Department", "Department")).append("\";");
                sb.append("\nfunction validatedetails() {return childsampleplan.validate();}");
                sql.setLength(0);
                safeSQL.reset();
                sql.append("select distinct destsampletypeid from s_preptypesampletypemap where sourcesampletypeid = ").append(safeSQL.addVar(parentsampletype)).append(" order by destsampletypeid");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds != null) {
                    for (int i2 = 0; i2 < ds.size(); ++i2) {
                        sb.append("\nchildsampleplan.childSampleTypes.push('").append(ds.getString(i2, "destsampletypeid")).append("');");
                    }
                }
                if ("N".equals(this.element.getPropertyListNotNull("template").getProperty("show", "Y"))) {
                    sb.append("\nchildsampleplan.allowTemplateSelection = false;");
                } else {
                    DataSet dsTemplates;
                    String templateRestrictiveWhere = this.element.getPropertyListNotNull("template").getProperty("restrictivewhere", "").trim();
                    SDIRequest sdiRequest = new SDIRequest();
                    sdiRequest.setRequestItem("primary");
                    sdiRequest.setSDCid("Sample");
                    sdiRequest.setShowTemplates(true);
                    sdiRequest.setQueryFrom("s_sample");
                    String whereclause = " templateflag = 'Y'";
                    if (StringUtil.getLen(templateRestrictiveWhere) > 0L) {
                        whereclause = whereclause + " and " + templateRestrictiveWhere;
                    }
                    sdiRequest.setQueryWhere(whereclause);
                    sdiRequest.setQueryOrderBy("s_sampleid");
                    SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
                    if (sdiData != null && (dsTemplates = sdiData.getDataset("primary")) != null) {
                        for (int i3 = 0; i3 < dsTemplates.size(); ++i3) {
                            sb.append("\nchildsampleplan.templateArray.push('").append(dsTemplates.getString(i3, "s_sampleid")).append("');");
                        }
                    }
                }
                PropertyList units = this.element.getPropertyListNotNull("units");
                String units_categoryid = units.getProperty("categoryid", "");
                String units_restrictivewhere = units.getProperty("restrictivewhere", "");
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setRequestItem("primary");
                sdiRequest.setSDCid("Units");
                sdiRequest.setQueryFrom("units");
                String whereclause = "";
                if (StringUtil.getLen(units_categoryid) > 0L) {
                    whereclause = " unitsid in ( select ci.keyid1 from categoryitem ci where ci.sdcid = 'Units' and ci.categoryid = '" + SafeSQL.encodeForSQL(units_categoryid, isOracle) + "' )";
                }
                if (StringUtil.getLen(units_restrictivewhere) > 0L) {
                    if (whereclause.length() > 0) {
                        whereclause = whereclause + " and ";
                    }
                    whereclause = whereclause + units_restrictivewhere;
                }
                if (StringUtil.getLen(whereclause) > 0L) {
                    sdiRequest.setQueryWhere(whereclause);
                }
                sdiRequest.setQueryOrderBy("unitsid");
                SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
                if (sdiData != null && (dsUnits = sdiData.getDataset("primary")) != null) {
                    for (i = 0; i < dsUnits.size(); ++i) {
                        sb.append("\nchildsampleplan.unitsArray.push('").append(dsUnits.getString(i, "unitsid")).append("');");
                    }
                }
                if ((processTypeDS = this.getQueryProcessor().getRefTypeDataSet("SpecimenProcessType")) != null) {
                    for (i = 0; i < processTypeDS.size(); ++i) {
                        String refvalue = processTypeDS.getString(i, "refvalueid");
                        sb.append("\nchildsampleplan.processType.push([\"").append(SafeHTML.encodeForJavaScript(refvalue)).append("\", \"").append(SafeHTML.encodeForJavaScript(processTypeDS.getString(i, "refdisplayvalue", refvalue))).append("\"]);");
                    }
                }
                if ((WICompletedStatusDS = this.getQueryProcessor().getRefTypeDataSet("WICompletedStatus")) != null) {
                    for (int i4 = 0; i4 < WICompletedStatusDS.size(); ++i4) {
                        String refvalue = WICompletedStatusDS.getString(i4, "refvalueid");
                        if (!typeIsBioBank && refvalue.equals("In Circulation")) continue;
                        sb.append("\nchildsampleplan.wiCompletedStatus.push([\"").append(SafeHTML.encodeForJavaScript(refvalue)).append("\", \"").append(SafeHTML.encodeForJavaScript(WICompletedStatusDS.getString(i4, "refdisplayvalue", refvalue))).append("\"]);");
                    }
                }
                Object[] key = new String[]{childsampleplanid, childsampleplanversionid};
                sql.setLength(0);
                sql.append("select c.s_childsampleplanid, c.s_childsampleplanversionid, c.s_childsampleplanitemid, c.workitemid, c.workitemversionid, c.workiteminstance, c.applyonaddflag,");
                sql.append(" c.embedchildsampleplanid, c.embedchildsampleplanversionid, c.assigneddepartmentid");
                for (String token : displaytokens = StringUtil.getTokens(workitemdisplayformat)) {
                    if ("workitemid".equals(token) || "workitemversionid".equals(token)) continue;
                    if (!token.contains(".")) {
                        sql.append(", c.").append(token);
                        continue;
                    }
                    sql.append(",").append(token).append(" ").append(StringUtil.replaceAll(token, ".", "_"));
                }
                sql.append(" from s_childsampleplanworkitem c");
                sql.append(" where c.S_CHILDSAMPLEPLANID = ? and c.S_CHILDSAMPLEPLANVERSIONID = ?");
                sql.append(" order by c.s_childsampleplanitemid, c.usersequence");
                DataSet wids = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), key);
                HashSet<String> sampleTypeSet = new HashSet<String>();
                sampleTypeSet.add(parentsampletype);
                sql.setLength(0);
                sql.append("select s_childsampleplanitemid, parentitemid, plantype, sampletemplateid, childsamplecount, derivativesampletypeid, derivativepreptypeid, ");
                sql.append(" derivativetreatmenttypeid, derivativeparentquantity, derivativeparentquantityunits, quantity, quantityunits, usersequence,");
                sql.append(" processtype, processinstruction, todepartmentid, labelmethodid, labelmethodversionid, containertypeid, sdiworkitemcompletionstatus");
                for (int col = 0; col < columns.size(); ++col) {
                    PropertyList columnProps = columns.getPropertyList(col);
                    String column = columnProps.getProperty("column").trim();
                    if (column.length() <= 0) continue;
                    sql.append(", ").append(column);
                }
                sql.append(" from s_childsampleplanitem");
                sql.append(" where s_childsampleplanid = ? and s_childsampleplanversionid = ?");
                sql.append(" order by usersequence");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), key);
                if (ds != null && ds.size() > 0) {
                    HashMap<String, String> filter = new HashMap<String, String>();
                    JSONArray jsonArray = new JSONArray();
                    for (int i5 = 0; i5 < ds.size(); ++i5) {
                        try {
                            String childsampleplanitemid = ds.getString(i5, "s_childsampleplanitemid", "");
                            String derivativesampletypeid = ds.getValue(i5, "derivativesampletypeid", "");
                            if (derivativesampletypeid.length() > 0) {
                                sampleTypeSet.add(derivativesampletypeid);
                            }
                            String processinstruction = ds.getValue(i5, "processinstruction", "");
                            processinstruction = StringUtil.replaceAll(processinstruction, "\"", "&quot;");
                            processinstruction = StringUtil.replaceAll(processinstruction, "'", "&#39;");
                            processinstruction = StringUtil.replaceAll(processinstruction, "\n", "\\n");
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("s_childsampleplanitemid", ds.getString(i5, "s_childsampleplanitemid", ""));
                            jsonObject.put("parentitemid", ds.getString(i5, "parentitemid", ""));
                            jsonObject.put("plantype", SafeHTML.encodeForHTML(ds.getString(i5, "plantype", "")));
                            jsonObject.put("sampletemplateid", ds.getString(i5, "sampletemplateid", ""));
                            jsonObject.put("childsamplecount", ds.getValue(i5, "childsamplecount", ""));
                            jsonObject.put("derivativesampletypeid", derivativesampletypeid);
                            jsonObject.put("derivativepreptypeid", ds.getValue(i5, "derivativepreptypeid", ""));
                            jsonObject.put("derivativetreatmenttypeid", ds.getValue(i5, "derivativetreatmenttypeid", ""));
                            jsonObject.put("derivativeparentquantity", ds.getValue(i5, "derivativeparentquantity", ""));
                            jsonObject.put("derivativeparentquantityunits", ds.getValue(i5, "derivativeparentquantityunits", ""));
                            jsonObject.put("quantity", ds.getValue(i5, "quantity", ""));
                            jsonObject.put("quantityunits", ds.getValue(i5, "quantityunits", ""));
                            jsonObject.put("usersequence", ds.getValue(i5, "usersequence", ""));
                            jsonObject.put("processtype", SafeHTML.encodeForHTML(ds.getValue(i5, "processtype", "")));
                            jsonObject.put("processinstruction", processinstruction);
                            jsonObject.put("todepartmentid", ds.getValue(i5, "todepartmentid", ""));
                            jsonObject.put("labelmethodid", ds.getValue(i5, "labelmethodid", ""));
                            jsonObject.put("labelmethodversionid", ds.getValue(i5, "labelmethodversionid", ""));
                            jsonObject.put("containertypeid", ds.getValue(i5, "containertypeid", ""));
                            jsonObject.put("sdiworkitemcompletionstatus", ds.getValue(i5, "sdiworkitemcompletionstatus", ""));
                            for (int col = 0; col < columns.size(); ++col) {
                                PropertyList columnProps = columns.getPropertyList(col);
                                String column = columnProps.getProperty("column");
                                if (column.length() <= 0) continue;
                                jsonObject.put(column, ds.getValue(i5, column, ""));
                            }
                            JSONArray wiarray = new JSONArray();
                            filter.put("s_childsampleplanitemid", childsampleplanitemid);
                            DataSet _ds = wids.getFilteredDataSet(filter);
                            if (_ds != null && _ds.size() > 0) {
                                for (int row = 0; row < _ds.size(); ++row) {
                                    String workitemid = _ds.getString(row, "workitemid");
                                    String workitemversionid = _ds.getString(row, "workitemversionid", "");
                                    String supportembeddedchildplanflag = "N";
                                    if (workitemversionid.length() > 0) {
                                        supportembeddedchildplanflag = OpalUtil.getColumnValue(this.getQueryProcessor(), "workitem", "supportembeddedchildplanflag", "workitemid=? and workitemversionid=?", new String[]{workitemid, workitemversionid});
                                    }
                                    JSONObject jsonObject1 = new JSONObject();
                                    jsonObject1.put("s_childsampleplanitemid", _ds.getString(row, "s_childsampleplanitemid"));
                                    jsonObject1.put("workitemid", workitemid);
                                    jsonObject1.put("workitemversionid", workitemversionid.length() == 0 ? "C" : workitemversionid);
                                    jsonObject1.put("workiteminstance", _ds.getInt(row, "workiteminstance"));
                                    jsonObject1.put("applyonaddflag", _ds.getString(row, "applyonaddflag"));
                                    jsonObject1.put("supportembeddedchildplanflag", supportembeddedchildplanflag);
                                    jsonObject1.put("embedchildsampleplanid", _ds.getString(row, "embedchildsampleplanid", ""));
                                    jsonObject1.put("embedchildsampleplanversionid", _ds.getString(row, "embedchildsampleplanversionid", ""));
                                    jsonObject1.put("viewversionid", workitemversionid);
                                    jsonObject1.put("assigneddepartmentid", _ds.getString(row, "assigneddepartmentid", ""));
                                    String displayvalue = workitemdisplayformat;
                                    for (String token : displaytokens) {
                                        if (token.contains(".")) {
                                            displayvalue = StringUtil.replaceAll(displayvalue, "[" + token + "]", _ds.getValue(row, StringUtil.replaceAll(token, ".", "_"), ""));
                                            continue;
                                        }
                                        String value = _ds.getValue(row, token, "");
                                        if (value.length() == 0 && "workitemversionid".equals(token)) {
                                            value = "C";
                                        }
                                        displayvalue = StringUtil.replaceAll(displayvalue, "[" + token + "]", value);
                                    }
                                    jsonObject1.put("displayvalue", displayvalue);
                                    wiarray.put(jsonObject1);
                                }
                            }
                            jsonObject.put("workitems", wiarray);
                            jsonArray.put(jsonObject);
                            continue;
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    JSONObject containers = new JSONObject();
                    for (String sampletypeid : sampleTypeSet) {
                        sdiRequest = new SDIRequest();
                        sdiRequest.setRequestItem("primary");
                        sdiRequest.setSDCid("ContainerType");
                        sdiRequest.setQueryFrom("containertype");
                        sdiRequest.setQueryWhere("containertype.containertypeid in (select s.containertypeid from s_sampletypecontainertype s where s.s_sampletypeid = '" + SafeSQL.encodeForSQL(sampletypeid, isOracle) + "' and (s.activeflag = 'Y' or s.activeflag is null))");
                        sdiRequest.setQueryOrderBy("containertypeid");
                        DataSet containerds = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
                        JSONArray containerArray = new JSONArray();
                        if (containerds != null) {
                            for (int i6 = 0; i6 < containerds.size(); ++i6) {
                                containerArray.put(containerds.getString(i6, "containertypeid"));
                            }
                        }
                        try {
                            containers.put(sampletypeid, containerArray);
                        }
                        catch (JSONException jSONException) {}
                    }
                    sb.append("\nchildsampleplan.sampletypecontainers = ").append(containers.toString()).append(";");
                    sb.append("\nchildsampleplan.initialize( '").append(jsonArray.toString()).append("' );");
                } else {
                    JSONObject containers = new JSONObject();
                    sdiRequest = new SDIRequest();
                    sdiRequest.setRequestItem("primary");
                    sdiRequest.setSDCid("ContainerType");
                    sdiRequest.setQueryFrom("containertype");
                    sdiRequest.setQueryWhere("containertype.containertypeid in (select s.containertypeid from s_sampletypecontainertype s where s.s_sampletypeid = '" + SafeSQL.encodeForSQL(parentsampletype, isOracle) + "' and (s.activeflag = 'Y' or s.activeflag is null))");
                    sdiRequest.setQueryOrderBy("containertypeid");
                    DataSet containerds = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
                    JSONArray containerArray = new JSONArray();
                    if (containerds != null) {
                        for (int i7 = 0; i7 < containerds.size(); ++i7) {
                            containerArray.put(containerds.getString(i7, "containertypeid"));
                        }
                    }
                    try {
                        containers.put(parentsampletype, containerArray);
                    }
                    catch (JSONException jSONException) {
                        // empty catch block
                    }
                    sb.append("\nchildsampleplan.sampletypecontainers = ").append(containers.toString()).append(";");
                }
                sb.append("</script>");
            } else {
                sb.append("<span style='color:red'>Error rendering the Child Sample Plan Items. No Child Sample Plan found for ").append(childsampleplanid).append(":").append(childsampleplanversionid).append(".</span>");
            }
        }
        return sb.toString();
    }

    protected String renderError(String error) {
        return "<span style='color:red'>" + this.getTranslationProcessor().translate(error) + "</span>";
    }
}

