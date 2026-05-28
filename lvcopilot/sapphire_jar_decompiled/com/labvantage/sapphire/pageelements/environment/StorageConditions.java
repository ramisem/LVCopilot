/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.environment;

import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.JstlUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class StorageConditions
extends BaseElement {
    @Override
    public String getHtml() {
        String lockedBy;
        DataSet primary;
        boolean readonly;
        boolean viewMode = false;
        StringBuilder html = new StringBuilder();
        TranslationProcessor tp = this.getTranslationProcessor();
        QueryProcessor qp = this.getQueryProcessor();
        HashMap refTypeHolder = new HashMap();
        SQLGenerator sqlGenerator = SQLFactory.getSqlGenerator(this.pageContext);
        PropertyList pagedata = (PropertyList)JstlUtil.evaluateExpression("${pagedata}", this.pageContext);
        boolean bl = readonly = this.element.getProperty("readonly") != null && this.element.getProperty("readonly").equals("Y");
        if (!readonly && (primary = this.sdiInfo.getDataSet("primary")) != null && (lockedBy = primary.getValue(0, "__lockedby", "")) != null && lockedBy.length() > 0) {
            readonly = true;
        }
        if ("View".equalsIgnoreCase(pagedata.getProperty("mode")) || readonly) {
            viewMode = true;
        }
        html.append("<script type=\"text/javascript\" src='WEB-CORE/elements/environment/scripts/storageconditions.js'></script>");
        html.append("<input type=\"hidden\" name=\"__propertyhandler_envcond\" value=\"com.labvantage.opal.pagetype.storage.EnvConditionHandler\">");
        html.append("<span style=\"color:brown; font-weight:normal\">").append(tp.translate("Select the conditions from the list below (use the checkbox) and enter values for the same")).append("</span>");
        html.append("<br><br>");
        html.append("<table cellpadding=\"2\" cellspacing=\"0\" class=\"maintform_table_blue\">");
        html.append("<tr>");
        html.append("<td colspan=\"3\" class=\"maintform_fieldtitle_blue\">").append(tp.translate("Storage Conditions")).append("</td>");
        html.append("</tr>");
        String appearance = "blue";
        SafeSQL safeSQL = sqlGenerator.getStorageEnvCondTypesSql(pagedata.getProperty("keyid1"));
        DataSet envData = qp.getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
        if (envData != null) {
            ArrayList<String> unitsList = new ArrayList<String>();
            if (envData.size() > 0) {
                int i;
                SDIProcessor sdiProcessor = new SDIProcessor(this.pageContext);
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("Units");
                sdiRequest.setQueryFrom("units");
                sdiRequest.setRequestItem("primary");
                SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
                DataSet dsUnits = sdiData.getDataset("primary");
                if (dsUnits != null) {
                    for (i = 0; i < dsUnits.size(); ++i) {
                        unitsList.add(dsUnits.getValue(i, "unitsid"));
                    }
                }
                for (i = 0; i < envData.size(); ++i) {
                    String valStorageCondTypeId = envData.getValue(i, "storagecondtypeid");
                    String dataTypeFlag = envData.getValue(i, "datatypeflag");
                    String storageCondTypeId = "envattribute" + i;
                    String storageCondId = envData.getValue(i, "storagecondid");
                    if (dataTypeFlag != null) {
                        String s;
                        int j;
                        List<String> list;
                        String reftypevalue;
                        String condRefType;
                        boolean selected = false;
                        String storageEnvId = envData.getValue(i, "storageenvid", "");
                        html.append("<tr>");
                        html.append("<td width=\"30\" valign=\"middle\" align=\"center\" class=\"maintform_field_").append("blue").append("\">");
                        html.append("<input type=\"checkbox\" name=\"storagecond_").append(storageCondTypeId).append("_selector\" id=\"storagecond_").append(storageCondTypeId).append("_selector\"");
                        if (storageEnvId != null && storageEnvId.length() > 0) {
                            html.append(" CHECKED");
                            selected = true;
                        }
                        if (viewMode) {
                            html.append(" DISABLED");
                        }
                        html.append(">");
                        html.append("<input type=\"hidden\" name=\"__storagecond_").append(storageCondTypeId).append("_id\" id=\"__storagecond_").append(storageCondTypeId).append("_id\"  value=\"").append(storageCondId).append("\">");
                        html.append("<input type=\"hidden\" name=\"rowstaus_").append(storageCondTypeId).append("\" id=\"rowstatus_").append(storageCondTypeId).append("\"  value=\"S\">");
                        html.append("<input type=\"hidden\" name=\"").append(storageCondTypeId).append("\" value=\"").append(valStorageCondTypeId).append("\">");
                        html.append("<td width=\"250\" class=\"maintform_field_").append("blue").append("\">").append(valStorageCondTypeId).append("</td>");
                        html.append("<td width=\"200\" class=\"maintform_field_").append("blue").append("\">");
                        if (dataTypeFlag.equals("N")) {
                            String condValue = envData.getValue(i, "condvalue", "");
                            String condUnit = envData.getValue(i, "condunits", "");
                            String defaultUnit = envData.getValue(i, "defaultunits", "");
                            html.append("\n<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
                            html.append("\n<tr><td>");
                            if (viewMode) {
                                html.append("&nbsp;").append(condValue);
                            } else {
                                html.append("<input style=\"width:100px\" value=\"").append(condValue).append("\"");
                                html.append(" id=\"storagecond_").append(storageCondTypeId).append("_condvalue\"");
                                html.append(" name=\"storagecond_").append(storageCondTypeId).append("_condvalue\"");
                                html.append(" class=\"validateNumber\" fieldlabel=\"").append(this.getTranslationProcessor().translate(valStorageCondTypeId)).append("\"");
                                html.append(" onChange=\"storageConditions.onValueChange(this.value, '").append(storageCondTypeId).append("');\">");
                            }
                            html.append("</td><td>&nbsp;");
                            if (viewMode) {
                                html.append("&nbsp;").append(condUnit);
                            } else {
                                html.append("<select name=\"storagecond_").append(storageCondTypeId).append("_condunits\" onChange=\"storageConditions.updateRowStatus('").append(storageCondTypeId).append("')\">");
                                html.append("<option></option>");
                                boolean selUnit = false;
                                for (Object e : unitsList) {
                                    String unit = (String)e;
                                    html.append("<option value=\"").append(unit).append("\"");
                                    if (selected) {
                                        if (condUnit.equals(unit)) {
                                            selUnit = true;
                                            html.append(" SELECTED");
                                        }
                                    } else if (defaultUnit.equals(unit)) {
                                        selUnit = true;
                                        html.append(" SELECTED");
                                    }
                                    html.append(">");
                                    html.append(unit);
                                    html.append("</option>");
                                }
                                if (!selUnit) {
                                    String unit = condUnit.length() > 0 ? condUnit : (defaultUnit.length() > 0 ? defaultUnit : "");
                                    html.append("<option value=\"").append(unit).append("\" SELECTED >");
                                    html.append(unit.length() > 0 ? "?-" + unit + "-?" : unit);
                                    html.append("</option>");
                                }
                                html.append("</select>");
                            }
                            html.append("</td></tr></table>");
                        } else if (dataTypeFlag.equals("T")) {
                            String condText = envData.getValue(i, "condtext", "");
                            if (viewMode) {
                                html.append("&nbsp;").append(condText);
                            } else {
                                html.append("<input id=\"storagecond_").append(storageCondTypeId).append("_condtext\" name=\"storagecond_").append(storageCondTypeId).append("_condtext\"");
                                html.append(" fieldlabel=\"").append(this.getTranslationProcessor().translate(valStorageCondTypeId)).append("\"");
                                html.append(" value=\"").append(condText).append("\" onChange=\"storageConditions.onValueChange(this.value, '").append(storageCondTypeId).append("');\">");
                            }
                        } else if (dataTypeFlag.equals("R")) {
                            condRefType = envData.getValue(i, "condreftype");
                            reftypevalue = envData.getValue(i, "condtext");
                            list = this.getRefType(qp, condRefType, refTypeHolder);
                            if (viewMode) {
                                html.append("&nbsp;").append(reftypevalue);
                            } else {
                                html.append("<select name=\"storagecond_").append(storageCondTypeId).append("_condtext\" onChange=\"storageConditions.onValueChange(this.options[this.selectedIndex].text, '").append(storageCondTypeId).append("');\">");
                                html.append("<option></option>");
                                for (j = 0; j < list.size(); ++j) {
                                    s = list.get(j);
                                    html.append("<option");
                                    if (reftypevalue.equals(s)) {
                                        html.append(" SELECTED");
                                    }
                                    html.append(">");
                                    html.append(s);
                                    html.append("</option>");
                                }
                                html.append("</select>");
                            }
                        } else if (dataTypeFlag.equals("V")) {
                            condRefType = envData.getValue(i, "condreftype");
                            reftypevalue = envData.getValue(i, "condtext");
                            list = this.getRefType(qp, condRefType, refTypeHolder);
                            if (viewMode) {
                                html.append("&nbsp;").append(reftypevalue);
                            } else {
                                html.append("<select name=\"storagecond_").append(storageCondTypeId).append("_condtext\" onChange=\"storageConditions.onValueChange(this.options[this.selectedIndex].text, '").append(storageCondTypeId).append("');\">");
                                html.append("<option></option>");
                                for (j = 0; j < list.size(); ++j) {
                                    s = list.get(j);
                                    html.append("<option");
                                    if (reftypevalue.equals(s)) {
                                        html.append(" SELECTED");
                                    }
                                    html.append(">");
                                    html.append(s);
                                    html.append("</option>");
                                }
                                html.append("</select>");
                            }
                        } else if (dataTypeFlag.equals("S")) {
                            html.append(envData.getValue(i, "condsdcid"));
                        } else {
                            html.append("&nbsp;");
                        }
                        html.append("</td>");
                        html.append("</tr>");
                        continue;
                    }
                    html.append("\n<tr>");
                    html.append("<td colspan=\"3\"><font color=\"red\">Condition DataType is not defined</font><td>");
                    html.append("</tr>");
                }
            } else {
                html.append("\n<tr><td class=\"maintform_field_").append("blue").append("\">");
                html.append("<font face=\"red\">").append(tp.translate("No Condition Types found")).append("</font>");
                html.append("</td></tr>");
            }
        }
        html.append("</table>");
        return html.toString();
    }

    private List<String> getRefType(QueryProcessor queryProcessor, String reftypeid, HashMap refTypeHolder) {
        if (!refTypeHolder.containsKey(reftypeid)) {
            DataSet ds = queryProcessor.getRefTypeDataSet(reftypeid);
            ArrayList<String> list = new ArrayList<String>();
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    list.add(ds.getValue(i, "refvalueid"));
                }
            }
            refTypeHolder.put(reftypeid, list);
        }
        return (List)refTypeHolder.get(reftypeid);
    }
}

