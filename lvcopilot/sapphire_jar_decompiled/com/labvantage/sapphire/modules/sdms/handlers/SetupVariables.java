/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.sdms.handlers;

import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.SDITagInfo;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SetupVariables
extends BaseElement {
    public static final String JS_CLASS = "setUpVariables";
    public static final String PROPERTY_PROPERTIES = "properties";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    private boolean viewonly = false;
    PropertyList dhprops = null;

    public SetupVariables(PageContext pageContext, PropertyList pageproperties) {
        this.setPageContext(pageContext);
        try {
            this.setUpProperties(pageproperties);
        }
        catch (Exception e) {
            this.dhprops = new PropertyList();
            this.logger.error("Could not set up painter: " + e.getMessage(), e);
        }
        this.logger.debug("Set up completed.");
    }

    private void setUpProperties(PropertyList pagedata) throws Exception {
        pagedata.setProperty("jsrequest", "exclude=properties");
        String show = pagedata.getProperty("showtitle", "N");
        this.logger.debug("show = " + show);
        PropertyList layout = pagedata.getPropertyList("layout");
        if (layout != null) {
            layout.setProperty("hideshadow", "Y");
            if (show.equalsIgnoreCase("N")) {
                layout.setProperty("hidetitle", "Y");
            } else {
                layout.setProperty("hidetitle", "N");
            }
        }
        this.viewonly = pagedata.getProperty(PROPERTY_VIEWONLY, "n").equalsIgnoreCase("y");
        this.logger.debug("viewonly = " + this.viewonly);
        String taskp = pagedata.getProperty(PROPERTY_PROPERTIES, "");
        if (taskp.length() > 0) {
            try {
                this.dhprops = new PropertyList(new JSONObject(taskp));
            }
            catch (Exception e) {
                this.dhprops = new PropertyList();
            }
        } else {
            this.dhprops = new PropertyList();
        }
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.dhprops != null) {
            html.append(this.getScriptAndStyle());
            html.append("<div id=\"dhsetupvariables_container\" style=\"height:auto;width:auto;overflow:auto;position:absolute;left:0;right:0;bottom:0;top:0;\">");
            html.append(SetupVariables.getVariableGrid(this.dhprops, this.viewonly, this.sdiInfo, this.getConnectionId(), this.getTranslationProcessor(), this.pageContext));
            html.append("</div>");
            html.append(this.getEndScript(this.viewonly));
        } else {
            html.append("<font color=\"red\">").append(this.getTranslationProcessor().translate("Could not load properties for variables.")).append("</font>");
        }
        return html.toString();
    }

    public static String getVariableGrid(PropertyList dhprops, boolean viewonly, SDITagInfo sdiInfo, String connectionId, TranslationProcessor translationProcessor, PageContext pageContext) {
        StringBuffer html = new StringBuffer();
        StringBuffer tabhtml = new StringBuffer();
        StringBuffer tab1 = new StringBuffer();
        PropertyListCollection variables = dhprops.getCollection("variables");
        html.append("<form name=\"dh_variablesform\" action=\"#\">");
        if (variables == null) {
            variables = new PropertyListCollection();
            dhprops.setProperty("variables", variables);
        }
        boolean foundVar = false;
        for (int i = 0; i < variables.size(); ++i) {
            PropertyList var = variables.getPropertyList(i);
            String varId = var.getProperty("variableid", "");
            String defaultvalue = var.getProperty("value", "");
            String varidrep = StringUtil.replaceAll(varId, " ", "_");
            String editorstyle = var.getProperty("editorstyleid", "");
            String prompt = var.getProperty("prompt", "");
            tabhtml.append("<tr>");
            if (!viewonly) {
                tabhtml.append("<td class=\"gridmaint_field\">");
                tabhtml.append("<input type=\"checkbox\" name=\"").append("variable").append("_selector").append("").append("\" id=\"__").append("variable").append(i).append("_").append(varidrep).append("\">");
                tabhtml.append("</td>");
            }
            tabhtml.append("<td id=\"").append("variable").append(i).append("_").append(varidrep).append("_titlecell\" class=\"gridmaint_field\" style=\"\">");
            tabhtml.append(varId).append("");
            tabhtml.append("</td>");
            foundVar = true;
            tabhtml.append("<td class=\"gridmaint_field\">").append(SetupVariables.getVariableField("value", "input", varidrep, i, defaultvalue, "string", "", "", viewonly, "", connectionId, pageContext, sdiInfo, translationProcessor).getHtml()).append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\" nowrap>").append(SetupVariables.getVariableField("editorstyleid", "lookup", varidrep, i, editorstyle, "string", "", "", viewonly, "", connectionId, pageContext, sdiInfo, translationProcessor).getHtml()).append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\">").append(SetupVariables.getVariableField("prompt", "input", varidrep, i, prompt, "string", "", "", viewonly, "", connectionId, pageContext, sdiInfo, translationProcessor).getHtml()).append("</td>");
            tabhtml.append("</tr>");
        }
        if (!viewonly) {
            Button btn = new Button(pageContext);
            btn.setId("btnAddDHSetupVar");
            btn.setAction("setUpVariables.buttons.addVar()");
            btn.setTip("Add Setup Variable");
            btn.setImg("WEB-CORE/images/png/Add.png");
            tab1.append(btn.getHtml());
            tab1.append("&nbsp;");
            btn = new Button(pageContext);
            btn.setId("btnDeleteDHSetupVar");
            btn.setAction("setUpVariables.buttons.deleteVar()");
            btn.setTip("Delete Setup Variable");
            btn.setDisabled(!foundVar);
            btn.setImg("WEB-CORE/images/png/Delete.png");
            tab1.append(btn.getHtml());
            tab1.append("&nbsp;");
            btn = new Button(pageContext);
            btn.setId("btnMoveUpDHSetupVar");
            btn.setAction("setUpVariables.buttons.moveVarUp(event)");
            btn.setTip("Move Setup Variable Up");
            btn.setDisabled(!foundVar);
            btn.setImg("WEB-CORE/images/gif/MoveUp.gif");
            tab1.append(btn.getHtml());
            tab1.append("&nbsp;");
            btn = new Button(pageContext);
            btn.setId("btnMoveDownTaskVar");
            btn.setAction("setUpVariables.buttons.moveVarDown(event)");
            btn.setTip("Move Setup Variable Down");
            btn.setDisabled(!foundVar);
            btn.setImg("WEB-CORE/images/gif/MoveDown.gif");
            tab1.append(btn.getHtml());
        }
        if (foundVar) {
            tab1.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" class=\"gridmaint_table\">");
            tab1.append("<thead>");
            tab1.append("<tr class=\"gridmaint_tablehead\">");
            if (!viewonly) {
                tab1.append("<th  class=\"gridmaint_fieldtitle\">");
                tab1.append("<input style=\"width:13px;height:13px\" onclick='setUpVariables.selectDeselectAll(this);' type=\"checkbox\" name=\"").append("variable").append("_selectdeselectall").append(" id=\"").append("variable").append("_selectdeselectall").append("\">");
                tab1.append("</th>");
            }
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(translationProcessor.translate("Variable")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\" style=\"width:180px;\">").append(translationProcessor.translate("Default Value")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(translationProcessor.translate("Editor Style")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(translationProcessor.translate("Prompt")).append("</th>");
            tab1.append("</tr>");
            tab1.append("</thead>");
            tab1.append("<tbody>");
            tab1.append(tabhtml);
            tab1.append("</tbody>");
            tab1.append("</table>");
            tab1.append("<br>");
        } else {
            tab1.append("<p>No SetUp Variables");
        }
        html.append("<section id=\"section_wv\" style=\"display:").append("block").append(";padding-bottom:15px;\">");
        html.append(tab1);
        html.append("</section>");
        html.append("</form>");
        return html.toString();
    }

    public static EditorStyleField getVariableField(String field, String mode, String variableid, int realrow, String value, String datatype, String relatedvariableid, String lookupcallback, boolean viewonly, String changeEvent, String connectionId, PageContext pageContext, SDITagInfo sdiInfo, TranslationProcessor tp) {
        PropertyListCollection llcols;
        EditorStyleField esf = new EditorStyleField(pageContext, sdiInfo, connectionId);
        esf.setDatasetname("attribute");
        PropertyList column = new PropertyList();
        column.setProperty("mode", mode);
        esf.setColumnDefinition(field, EditorStyleField.getEditorStyleDataType("C"), 1000, false);
        if (mode.equalsIgnoreCase("checkbox")) {
            column.setProperty("displayvalue", "Y=Yes;N=No");
            if (viewonly) {
                esf.setReadonly(true);
            }
        } else if (mode.equalsIgnoreCase("lookup") && field.equalsIgnoreCase("editorstyleid")) {
            if (!viewonly) {
                PropertyList lookuplink = new PropertyList();
                lookuplink.setProperty("href", "rc?command=page&page=LV_EditorStyleLookup");
                lookuplink.setProperty("restrictivewhere", "datatype='" + EditorStyleField.getEditorStyleDataType(datatype) + "'");
                lookuplink.setProperty("tip", tp.translate("Lookup Editor Style"));
                lookuplink.setProperty("dialogtype", "Sapphire Dialog");
                column.setProperty("lookuplink", lookuplink);
                column.setProperty("size", "15");
                esf.setReadonly(true);
                esf.setLinkDefinition("LV_EditorStyle", 'F', "", false);
            } else {
                column.setProperty("mode", "input");
                esf.setReadonly(true);
            }
            column.setProperty("size", "15");
        } else if (viewonly) {
            column.setProperty("mode", "readonly");
        } else if (relatedvariableid.length() > 0 && column.getPropertyList("lookuplink") != null && (llcols = column.getPropertyList("lookuplink").getCollection("columns")) != null && llcols.size() > 0) {
            String[] mapfields = StringUtil.split(relatedvariableid, ";");
            int mf = 0;
            boolean first = true;
            for (int i = 0; i < llcols.size(); ++i) {
                PropertyList llcol = llcols.getPropertyList(i);
                String llmode = llcol.getProperty("mode", "Display and Return");
                if (!llmode.equalsIgnoreCase("Display and Return") && !llmode.equalsIgnoreCase("Return Only")) continue;
                if (first) {
                    llcol.setProperty("mapfieldid", "defaultvalue");
                    first = false;
                    continue;
                }
                llcol.setProperty("mapfieldid", mapfields[mf]);
                if (++mf >= mapfields.length) break;
            }
            if (lookupcallback.length() > 0) {
                column.getPropertyList("lookuplink").setProperty("lookupcallback", lookupcallback);
            }
        }
        esf.setColumn(column);
        esf.setFieldName("variable" + realrow + "_" + field);
        if (changeEvent.length() == 0) {
            esf.setChangeEvent("setUpVariables.maintFieldChange(event,this,'" + variableid + "'," + realrow + ")");
        } else {
            esf.setChangeEvent(changeEvent);
        }
        esf.setUseNoNameAttribute(true);
        esf.setFieldValue(value);
        return esf;
    }

    private StringBuffer getScriptAndStyle() {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/scripts/tags.js\"></script>");
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/sdms/scripts/setupvariables.js\"></script>");
        html.append("\n<style>");
        html.append("\n.task_longtext { width:400px; }");
        html.append("\n</style>");
        return html;
    }

    private StringBuffer getEndScript(boolean viewOnly) {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\">");
        html.append(JS_CLASS).append(".viewonly=").append(viewOnly).append(";");
        html.append("</script>");
        return html;
    }
}

