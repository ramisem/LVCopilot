/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.forms;

import com.labvantage.sapphire.pageelements.forms.FormBuilder;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class FormBuilderPropertiesAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 76666 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "FormBuilderHandler");
        if (ajaxResponse.getRequestParameter("fielddialog").equalsIgnoreCase("Y")) {
            PropertyList defaultProps;
            String dp = ajaxResponse.getRequestParameter("defaultproperties");
            if (dp.length() > 0) {
                try {
                    defaultProps = new PropertyList(new JSONObject(dp));
                }
                catch (Exception e) {
                    defaultProps = new PropertyList();
                }
            } else {
                defaultProps = new PropertyList();
            }
            StringBuffer html = new StringBuffer();
            String change = " onchange=\"htmlEditor.plugins.fieldDialogPropertyChange(this, event)\" ";
            String type = ajaxResponse.getRequestParameter("type");
            html.append("<div class=\"mce-abs-end\"></div>");
            html.append("<div id=\"lvu_1\" class=\"mce-container mce-abs-layout-item mce-first mce-formitem\" hidefocus=\"1\" tabindex=\"-1\" style=\"left: 20px; top: 20px; width: 294px; height: 30px;\">");
            html.append("<div id=\"lvu_1-body\" class=\"mce-container-body mce-abs-layout\" style=\"width: 294px; height: 30px;\">");
            html.append("<div class=\"mce-abs-end\"></div>");
            html.append("<label id=\"fieldd_mandatory-l\" class=\"mce-widget mce-label mce-abs-layout-item mce-first\" for=\"fieldd_mandatory\" style=\"line-height: 16px; left: 0px; top: 7px; width: 114px; height: 16px;\">Mandatory</label>");
            html.append("<select").append(change).append("id=\"fieldd_mandatory\" name=\"mandatory\" class=\"mce-textbox mce-abs-layout-item mce-last\" style=\"left:114px;width:170px;\">");
            html.append("<option value=\"\" ").append(defaultProps.getProperty("mandatory").length() == 0 ? "SELECTED" : "").append("></option>");
            html.append("<option value=\"Y\" ").append(defaultProps.getProperty("mandatory").equalsIgnoreCase("Y") ? "SELECTED" : "").append(">Yes</option>");
            html.append("<option value=\"N\" ").append(defaultProps.getProperty("mandatory").equalsIgnoreCase("N") ? "SELECTED" : "").append(">No</option>");
            html.append("<option value=\"V\" ").append(defaultProps.getProperty("mandatory").equalsIgnoreCase("V") ? "SELECTED" : "").append(">If Visible</option>");
            html.append("<option value=\"E\"").append(defaultProps.getProperty("mandatory").equalsIgnoreCase("E") ? "SELECTED" : "").append(">If Enabled</option>");
            html.append("</select>");
            html.append("</div>");
            html.append("</div>");
            html.append("<div class=\"mce-abs-end\"></div>");
            html.append("<div id=\"lvu_2\" class=\"mce-container mce-abs-layout-item mce-first mce-formitem\" hidefocus=\"1\" tabindex=\"-1\" style=\"left: 20px; top: 60px; width: 294px; height: 30px;\">");
            html.append("<div id=\"lvu_2-body\" class=\"mce-container-body mce-abs-layout\" style=\"width: 294px; height: 30px;\">");
            html.append("<div class=\"mce-abs-end\"></div>");
            html.append("<label id=\"fieldd_datatype-l\" class=\"mce-widget mce-label mce-abs-layout-item mce-first\" for=\"fieldd_datatype\" style=\"line-height: 16px; left: 0px; top: 7px; width: 114px; height: 16px;\">Data Type</label>");
            html.append("<select").append(change).append("id=\"fieldd_datatype\" name=\"datatype\" class=\"mce-textbox mce-abs-layout-item mce-last\" style=\"left:114px;width:170px;\">");
            html.append("<option value=\"\" ").append(defaultProps.getProperty("datatype").length() == 0 ? "SELECTED" : "").append("></option>");
            html.append("<option value=\"string\" ").append(defaultProps.getProperty("datatype").equalsIgnoreCase("string") ? "SELECTED" : "").append(">string</option>");
            html.append("<option value=\"number\" ").append(defaultProps.getProperty("datatype").equalsIgnoreCase("number") ? "SELECTED" : "").append(">number</option>");
            html.append("<option value=\"date\" ").append(defaultProps.getProperty("datatype").equalsIgnoreCase("date") ? "SELECTED" : "").append(">date</option>");
            html.append("<option value=\"dateonly\" ").append(defaultProps.getProperty("datatype").equalsIgnoreCase("dateonly") ? "SELECTED" : "").append(">dateonly</option>");
            html.append("</select>");
            html.append("</div>");
            html.append("</div>");
            if (type.equalsIgnoreCase("lookup")) {
                html.append("<div class=\"mce-abs-end\"></div>");
                html.append("<div id=\"lvu_3\" class=\"mce-container mce-abs-layout-item mce-first mce-formitem\" hidefocus=\"1\" tabindex=\"-1\" style=\"left: 20px; top: 100px; width: 294px; height: 30px;\">");
                html.append("<div id=\"lvu_3-body\" class=\"mce-container-body mce-abs-layout\" style=\"width: 294px; height: 30px;\">");
                html.append("<div class=\"mce-abs-end\"></div>");
                html.append("<label id=\"fieldd_lookuppageid-l\" class=\"mce-widget mce-label mce-abs-layout-item mce-first\" for=\"fieldd_lookuppageid\" style=\"line-height: 16px; left: 0px; top: 7px; width: 114px; height: 16px;\">Lookup Page</label>");
                html.append("<input value=\"").append(defaultProps.getProperty("lookuppageid")).append("\"").append(change).append(" name=\"lookuppageid\" id=\"fieldd_lookuppageid\" hidefocus=\"1\" class=\"mce-textbox mce-abs-layout-item mce-last\" aria-labelledby=\"mceu_72-l\" style=\"left: 114px; top: 0px; width: 140px; height: 28px;\">");
                html.append("<button class=\"mce-textbox mce-abs-layout-item mce-last\" style=\"height:28px;width:20px;padding-left:5px;left: 255px; top: 0px;\" onclick=\"if(typeof(formBuilder)!='undefined')formBuilder.editURL('fieldd_lookuppageid');\">...</button>");
                html.append("</div>");
                html.append("</div>");
            } else if (type.equalsIgnoreCase("dropdown")) {
                html.append("<div class=\"mce-abs-end\"></div>");
                html.append("<div id=\"lvu_3\" class=\"mce-container mce-abs-layout-item mce-first mce-formitem\" hidefocus=\"1\" tabindex=\"-1\" style=\"left: 20px; top: 100px; width: 294px; height: 30px;\">");
                html.append("<div id=\"lvu_3-body\" class=\"mce-container-body mce-abs-layout\" style=\"width: 294px; height: 30px;\">");
                html.append("<div class=\"mce-abs-end\"></div>");
                html.append("<label id=\"fieldd_sdcid-l\" class=\"mce-widget mce-label mce-abs-layout-item mce-first\" for=\"fieldd_sdcid\" style=\"line-height: 16px; left: 0px; top: 7px; width: 114px; height: 16px;\">SDC Id</label>");
                html.append("<select").append(change).append("id=\"fieldd_sdcid\" name=\"sdcid\" class=\"mce-textbox mce-abs-layout-item mce-last\" style=\"left:114px;width:170px;\">");
                html.append("<option value=\"\" ").append(defaultProps.getProperty("datatype").equalsIgnoreCase("string") ? "SELECTED" : "").append("></option>");
                DataSet sdcs = this.getQueryProcessor().getSqlDataSet("SELECT sdcid FROM sdc");
                if (sdcs != null && sdcs.size() > 0) {
                    for (int i = 0; i < sdcs.size(); ++i) {
                        String v = sdcs.getValue(i, "sdcid", "");
                        html.append("<option value=\"").append(v).append("\" ").append(defaultProps.getProperty("sdcid").equalsIgnoreCase(v) ? "SELECTED" : "").append(">").append(v).append("</option>");
                    }
                }
                html.append("</select>");
                html.append("</div>");
                html.append("</div>");
                html.append("<div class=\"mce-abs-end\"></div>");
                html.append("<div id=\"lvu_4\" class=\"mce-container mce-abs-layout-item mce-first mce-formitem\" hidefocus=\"1\" tabindex=\"-1\" style=\"left: 20px; top: 140px; width: 294px; height: 30px;\">");
                html.append("<div id=\"lvu_4-body\" class=\"mce-container-body mce-abs-layout\" style=\"width: 294px; height: 30px;\">");
                html.append("<div class=\"mce-abs-end\"></div>");
                html.append("<label id=\"fieldd_reftypeid-l\" class=\"mce-widget mce-label mce-abs-layout-item mce-first\" for=\"fieldd_reftypeid\" style=\"line-height: 16px; left: 0px; top: 7px; width: 114px; height: 16px;\">Ref Type Id</label>");
                html.append("<select").append(change).append("id=\"fieldd_reftypeid\" name=\"reftypeid\" class=\"mce-textbox mce-abs-layout-item mce-last\" style=\"left:114px;width:170px;\">");
                html.append("<option value=\"\" ").append(defaultProps.getProperty("reftypeid").length() == 0 ? "SELECTED" : "").append("></option>");
                DataSet reftypes = this.getQueryProcessor().getSqlDataSet("SELECT reftypeid FROM reftype");
                if (reftypes != null && reftypes.size() > 0) {
                    for (int i = 0; i < reftypes.size(); ++i) {
                        String v = reftypes.getValue(i, "reftypeid", "");
                        html.append("<option value=\"").append(v).append("\" ").append(defaultProps.getProperty("reftypeid").equalsIgnoreCase(v) ? "SELECTED" : "").append(">").append(v).append("</option>");
                    }
                }
                html.append("</select>");
                html.append("</div>");
                html.append("</div>");
                html.append("<div class=\"mce-abs-end\"></div>");
                html.append("<div id=\"lvu_5\" class=\"mce-container mce-abs-layout-item mce-first mce-formitem\" hidefocus=\"1\" tabindex=\"-1\" style=\"left: 20px; top: 180px; width: 294px; height: 30px;\">");
                html.append("<div id=\"lvu_5-body\" class=\"mce-container-body mce-abs-layout\" style=\"width: 294px; height: 30px;\">");
                html.append("<div class=\"mce-abs-end\"></div>");
                html.append("<label id=\"fieldd_values-l\" class=\"mce-widget mce-label mce-abs-layout-item mce-first\" for=\"fieldd_values\" style=\"line-height: 16px; left: 0px; top: 7px; width: 114px; height: 16px;\">Values</label>");
                html.append("<input value=\"").append(defaultProps.getProperty("values")).append("\"").append(change).append(" name=\"values\" id=\"fieldd_values\" hidefocus=\"1\" class=\"mce-textbox mce-abs-layout-item mce-last\" aria-labelledby=\"mceu_72-l\" style=\"left: 114px; top: 0px; width: 170px; height: 28px;\">");
                html.append("</div>");
                html.append("</div>");
            } else if (type.equalsIgnoreCase("checkbox")) {
                html.append("<div class=\"mce-abs-end\"></div>");
                html.append("<div id=\"lvu_5\" class=\"mce-container mce-abs-layout-item mce-first mce-formitem\" hidefocus=\"1\" tabindex=\"-1\" style=\"left: 20px; top: 100px; width: 294px; height: 30px;\">");
                html.append("<div id=\"lvu_5-body\" class=\"mce-container-body mce-abs-layout\" style=\"width: 294px; height: 30px;\">");
                html.append("<div class=\"mce-abs-end\"></div>");
                html.append("<label id=\"fieldd_values-l\" class=\"mce-widget mce-label mce-abs-layout-item mce-first\" for=\"fieldd_values\" style=\"line-height: 16px; left: 0px; top: 7px; width: 114px; height: 16px;\">Values</label>");
                html.append("<input value=\"").append(defaultProps.getProperty("values")).append("\"").append(change).append(" name=\"values\" id=\"fieldd_values\" hidefocus=\"1\" class=\"mce-textbox mce-abs-layout-item mce-last\" aria-labelledby=\"mceu_72-l\" style=\"left: 114px; top: 0px; width: 170px; height: 28px;\">");
                html.append("</div>");
                html.append("</div>");
            } else if (type.equalsIgnoreCase("radiobutton")) {
                html.append("<div class=\"mce-abs-end\"></div>");
                html.append("<div id=\"lvu_3\" class=\"mce-container mce-abs-layout-item mce-first mce-formitem\" hidefocus=\"1\" tabindex=\"-1\" style=\"left: 20px; top: 100px; width: 294px; height: 30px;\">");
                html.append("<div id=\"lvu_3-body\" class=\"mce-container-body mce-abs-layout\" style=\"width: 294px; height: 30px;\">");
                html.append("<div class=\"mce-abs-end\"></div>");
                html.append("<label id=\"fieldd_reftypeid-l\" class=\"mce-widget mce-label mce-abs-layout-item mce-first\" for=\"fieldd_reftypeid\" style=\"line-height: 16px; left: 0px; top: 7px; width: 114px; height: 16px;\">Ref Type Id</label>");
                html.append("<select").append(change).append("id=\"fieldd_reftypeid\" name=\"reftypeid\" class=\"mce-textbox mce-abs-layout-item mce-last\" style=\"left:114px;width:170px;\">");
                html.append("<option value=\"\" ").append(defaultProps.getProperty("reftypeid").length() == 0 ? "SELECTED" : "").append("></option>");
                DataSet reftypes = this.getQueryProcessor().getSqlDataSet("SELECT reftypeid FROM reftype");
                if (reftypes != null && reftypes.size() > 0) {
                    for (int i = 0; i < reftypes.size(); ++i) {
                        String v = reftypes.getValue(i, "reftypeid", "");
                        html.append("<option value=\"").append(v).append("\" ").append(defaultProps.getProperty("reftypeid").equalsIgnoreCase(v) ? "SELECTED" : "").append(">").append(v).append("</option>");
                    }
                }
                html.append("</select>");
                html.append("</div>");
                html.append("</div>");
                html.append("<div class=\"mce-abs-end\"></div>");
                html.append("<div id=\"lvu_5\" class=\"mce-container mce-abs-layout-item mce-first mce-formitem\" hidefocus=\"1\" tabindex=\"-1\" style=\"left: 20px; top: 140px; width: 294px; height: 30px;\">");
                html.append("<div id=\"lvu_5-body\" class=\"mce-container-body mce-abs-layout\" style=\"width: 294px; height: 30px;\">");
                html.append("<div class=\"mce-abs-end\"></div>");
                html.append("<label id=\"fieldd_values-l\" class=\"mce-widget mce-label mce-abs-layout-item mce-first\" for=\"fieldd_values\" style=\"line-height: 16px; left: 0px; top: 7px; width: 114px; height: 16px;\">Values</label>");
                html.append("<input value=\"").append(defaultProps.getProperty("values")).append("\" ").append(change).append(" name=\"values\" id=\"fieldd_values\" hidefocus=\"1\" class=\"mce-textbox mce-abs-layout-item mce-last\" aria-labelledby=\"mceu_72-l\" style=\"left: 114px; top: 0px; width: 170px; height: 28px;\">");
                html.append("</div>");
                html.append("</div>");
            }
            ajaxResponse.addCallbackArgument("html", html);
        } else {
            String props = ajaxResponse.getRequestParameter("formproperties", "");
            if (props.length() > 0) {
                FormBuilder.Mode buildMode = FormBuilder.Mode.getMode(ajaxResponse.getRequestParameter("buildmode"));
                try {
                    PropertyList formprop = new PropertyList(new JSONObject(props));
                    String fieldid = ajaxResponse.getRequestParameter("fieldid", "");
                    String groupid = ajaxResponse.getRequestParameter("groupid", "");
                    String sectionid = ajaxResponse.getRequestParameter("sectionid", "");
                    String pageid = ajaxResponse.getRequestParameter("pageid", "");
                    String labelid = ajaxResponse.getRequestParameter("labelid", "");
                    String datasourceid = ajaxResponse.getRequestParameter("datasourceid", "");
                    String elementid = ajaxResponse.getRequestParameter("elementid", "");
                    try {
                        PropertyList userConf = RequestContext.getInstance(request).getPropertyList("userconfig");
                        if (userConf != null && !ajaxResponse.getRequestParameter("propertyGrouping", "").equalsIgnoreCase(userConf.getProperty("propertybuilder_groupby", ""))) {
                            userConf.setProperty("propertybuilder_groupby", ajaxResponse.getRequestParameter("propertyGrouping", "cat"));
                        }
                        String propsHtml = FormBuilder.getPropertiesHtml(datasourceid, fieldid, groupid, sectionid, pageid, labelid, elementid, formprop, ajaxResponse.getRequestParameter("viewonly", "n").equalsIgnoreCase("y"), buildMode, this.getConnectionId(), userConf, this.getTranslationProcessor(), request.getSession(), ProcessingUtil.createBindingsMap(null, this.getQueryProcessor(), this.getSDCProcessor(), null, null));
                        ajaxResponse.addCallbackArgument("html", propsHtml);
                    }
                    catch (Exception e) {
                        ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain properties HTML."));
                    }
                }
                catch (Exception e2) {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain propertylist from string provided."));
                }
            } else {
                ajaxResponse.setError(this.getTranslationProcessor().translate("No build mode provided."));
            }
        }
        ajaxResponse.print();
    }
}

