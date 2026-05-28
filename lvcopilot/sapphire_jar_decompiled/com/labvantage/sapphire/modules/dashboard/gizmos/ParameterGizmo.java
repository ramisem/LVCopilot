/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseGizmo;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ParameterGizmo
extends BaseGizmo {
    private static final String LABVANTAGE_CVS_ID = "";

    @Override
    public boolean init() {
        this.setRefreshOnResize(true);
        this.setTimeout(-1);
        return true;
    }

    @Override
    public String getHtml() {
        TranslationProcessor translationProcessor = null;
        translationProcessor = this.pageContext == null ? new TranslationProcessor(this.getConnectionid()) : this.getTranslationProcessor();
        StringBuffer html = new StringBuffer();
        if (this.element != null) {
            PropertyListCollection parameterRows = this.element.getCollection("parameters");
            html.append("<div style=\"padding-top:5px; padding-left:5px;\">");
            html.append("<table class=\"maintform_table_blue\" border=0>");
            html.append("<tbody>");
            if (parameterRows != null) {
                int colspan = 0;
                for (int r = 0; r < parameterRows.size(); ++r) {
                    PropertyListCollection parametersCol = parameterRows.getPropertyList(r).getCollection("row");
                    if (parametersCol == null) continue;
                    int current_colspan = 0;
                    for (int i = 0; i < parametersCol.size(); ++i) {
                        current_colspan += 2;
                    }
                    if (current_colspan <= colspan) continue;
                    colspan = current_colspan;
                }
                PropertyList parameters = this.getParameters();
                if (parameters == null || !parameters.containsKey("paramitems")) {
                    parameters = new PropertyList();
                    parameters.setProperty("paramitems", new PropertyListCollection());
                }
                PropertyListCollection rootparams = parameters.getCollection("paramitems");
                for (int r = 0; r < parameterRows.size(); ++r) {
                    html.append("<tr>");
                    PropertyListCollection parametersCol = parameterRows.getPropertyList(r).getCollection("row");
                    if (parametersCol != null) {
                        int i;
                        int current_col = 0;
                        for (i = 0; i < parametersCol.size(); ++i) {
                            String param = parametersCol.getPropertyList(i).getProperty("parameterid");
                            String title = parametersCol.getPropertyList(i).getProperty("title", param);
                            html.append("<td class=\"maintform_fieldtitle_blue\">").append(translationProcessor != null ? translationProcessor.translate(title) : title).append("</td>");
                            String def = parametersCol.getPropertyList(i).getProperty("defaultvalue");
                            PropertyList paramProps = rootparams.find("paramid", param);
                            if (paramProps == null) {
                                paramProps = new PropertyList();
                                paramProps.setProperty("paramid", param);
                                paramProps.setProperty("title", title);
                                paramProps.setProperty("value", def);
                                rootparams.add(paramProps);
                            } else if (def.length() == 0) {
                                def = paramProps.getProperty("value");
                            } else if (paramProps.getProperty("value").length() > 0) {
                                def = paramProps.getProperty("value");
                            } else {
                                paramProps.setProperty("value", def);
                            }
                            ++current_col;
                            EditorStyleField esf = new EditorStyleField(this.pageContext, null, this.getConnectionId());
                            String e = parametersCol.getPropertyList(i).getProperty("editorstyleid", LABVANTAGE_CVS_ID);
                            if (e.length() > 0) {
                                try {
                                    esf.setEditorStyleId(e);
                                }
                                catch (Exception exception) {}
                            } else {
                                esf.setDefaultEditorStyleProperties(EditorStyleField.getEditorStyleDataType("C"), LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID);
                            }
                            esf.setFieldName(this.elementid + "_" + param);
                            esf.setFieldValue(def);
                            esf.setColumnDefinition(param, esf.getEditorStyleDataType(), esf.getEditorStyleDataType().equalsIgnoreCase("c") ? 4000 : 255, false, esf.getEditorStyleDataType().equalsIgnoreCase("o"));
                            paramProps.setProperty("datatype", EditorStyleField.getColumnDataType(esf.getColumnProperty("editorstyledatatype")));
                            esf.setChangeEvent(this.elementid + "Change(event, this,'" + param + "');");
                            if (parametersCol.getPropertyList(i).getProperty("readonly", "N").equalsIgnoreCase("Y")) {
                                esf.setReadonly(true);
                            }
                            html.append("<td class=\"maintform_field_blue\">").append(esf.getHtml()).append("</td>");
                            ++current_col;
                        }
                        if (current_col < colspan) {
                            for (i = current_col; i < colspan; ++i) {
                                html.append("<td>").append("&nbsp;").append("</td>");
                            }
                        }
                    }
                    html.append("</tr>");
                }
            }
            html.append("</tbody>");
            html.append("</table>");
            html.append("</div>");
            StringBuffer script = new StringBuffer();
            if (this.getGizmoLocation() == BaseGizmo.GizmoLocation.DASHBOARD) {
                script.append("function ").append(this.elementid).append("Change(event, element, paramid){");
                script.append("try{");
                script.append("groupGizmo.parameters.set($(element).parents('.dashboardparameters').attr('gridid'),paramid, element.value,'" + this.elementid + "');");
                script.append("}catch(de){");
                script.append("sapphire.logger.error('Parameter Update Failed(1)');");
                script.append("sapphire.logger.error(de);");
                script.append("sapphire.alert('Unable to update parameter');");
                script.append("}");
                script.append("}");
            } else {
                PropertyListCollection col = this.element.getCollection("refresh");
                script.append("function ").append(this.elementid).append("Change(event, element, paramid){");
                script.append("try{");
                script.append("dashboard.updateParameter(paramid, element.value);");
                script.append("}catch(de){");
                script.append("sapphire.logger.error('Parameter Update Failed(1)');");
                script.append("sapphire.logger.error(de);");
                script.append("sapphire.alert('Unable to update parameter');");
                script.append("}");
                if (col != null) {
                    script.append("try{");
                    for (int i = 0; i < col.size(); ++i) {
                        String giz = col.getPropertyList(i).getProperty("gizmoid", LABVANTAGE_CVS_ID);
                        if (giz.length() <= 0) continue;
                        script.append("dashboard.refreshGizmo( dashboard.currentTab, '").append(giz).append("', false );");
                    }
                }
                script.append("}catch(e){}");
                script.append("}");
            }
            html.append("<script>").append(script).append("</script>");
        } else {
            html.append("<font size=2>").append(translationProcessor.translate("No element data found.")).append("</font>");
        }
        return html.toString();
    }

    @Override
    public String getScript() {
        return LABVANTAGE_CVS_ID;
    }

    @Override
    public String getIcon() {
        return this.getImage("Parameters", this.getGizmoStyle().size).getHtml();
    }

    @Override
    public String getDefaultImageSrc() {
        return "FormYellow";
    }

    @Override
    public String getIconHtml() {
        StringBuffer html = new StringBuffer();
        html.append(super.getIconHtml());
        html.append("<div style=\"display:none;\">" + this.getHtml() + "</div>");
        return html.toString();
    }
}

