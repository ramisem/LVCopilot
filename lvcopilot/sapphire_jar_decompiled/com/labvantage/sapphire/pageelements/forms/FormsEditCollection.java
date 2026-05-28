/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.forms;

import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.forms.PropertyBuilder;
import com.labvantage.sapphire.util.http.HttpUtil;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class FormsEditCollection
extends BaseElement {
    public FormsEditCollection(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    public FormsEditCollection() {
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        TranslationProcessor tp = this.getTranslationProcessor();
        try {
            String connectionId;
            if (this.requestContext == null) {
                this.requestContext = RequestContext.getRequestContext(this.pageContext);
            }
            if ((connectionId = this.requestContext.getConnectionId()) != null && connectionId.length() > 0) {
                this.setConnectionId(connectionId);
                String propstring = this.pageContext.getRequest().getParameter("properties");
                if (propstring != null && propstring.length() > 0) {
                    JSONObject job = new JSONObject(propstring);
                    PropertyList props = new PropertyList(job);
                    PropertyListCollection collection = props.getCollection("collection");
                    if (collection != null) {
                        PropertyListCollection inputs = props.getCollection("inputs");
                        if (inputs != null && inputs.size() > 0) {
                            String selected = this.pageContext.getRequest().getParameter("selected");
                            if (selected == null) {
                                selected = "__item0_div";
                            }
                            this.renderScriptAndStyle(html);
                            if (this.pageContext.getRequest().getParameter("title") != null && this.pageContext.getRequest().getParameter("title").length() > 0) {
                                html.append("<div style=\"font-size:9pt;padding-bottom:5px;padding-top:2px;\">").append(this.pageContext.getRequest().getParameter("title")).append("</div>");
                            }
                            if (collection.size() > 0) {
                                StringBuffer left = new StringBuffer();
                                StringBuffer right = new StringBuffer();
                                for (int i = 0; i < collection.size(); ++i) {
                                    PropertyList item = collection.getPropertyList(i);
                                    this.renderItem(left, selected, this.getTranslationProcessor().translate("Item") + " " + i, "__item" + i, i);
                                    this.renderInputs(right, item, inputs, selected, "__item" + i, i);
                                }
                                html.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"table_container\">");
                                html.append("<tbody>");
                                html.append("<tr>");
                                html.append("<td style=\"width:190px;\" valign=\"top\" align=\"left\">");
                                html.append("<table cellpadding=\"2\" cellspacing=\"2\" class=\"table_container\">");
                                html.append("<tbody>");
                                html.append(left);
                                html.append("<tr>");
                                html.append("<td colspan=\"2\" align=\"right\" valign=\"center\">");
                                this.renderButtons(html);
                                html.append("</td>");
                                html.append("</tr>");
                                html.append("</tbody>");
                                html.append("</table>");
                                html.append("</td>");
                                html.append("<td class=\"cell_right\">");
                                html.append(right);
                                html.append("</td>");
                                html.append("</tr>");
                                html.append("</tbody>");
                                html.append("</table>");
                            } else {
                                html.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
                                html.append("<tbody>");
                                html.append("<tr>");
                                html.append("<td>");
                                html.append("&nbsp;");
                                html.append("</td>");
                                html.append("</tr>");
                                html.append("<tr>");
                                html.append("<td align=\"center\" valign=\"center\">");
                                html.append(this.getTranslationProcessor().translate("No Items"));
                                html.append("</td>");
                                html.append("</tr>");
                                html.append("<tr>");
                                html.append("<td>");
                                html.append("&nbsp;");
                                html.append("</td>");
                                html.append("</tr>");
                                html.append("<tr>");
                                html.append("<td align=\"center\" valign=\"center\">");
                                this.renderButtons(html);
                                html.append("</td>");
                                html.append("</tr>");
                                html.append("</tbody>");
                                html.append("</table>");
                            }
                            this.renderEndScript(html, props, "__item", selected);
                            this.renderForm(html, selected);
                        } else {
                            html.append(tp.translate("No structure defined."));
                        }
                    } else {
                        html.append(tp.translate("No collection defined."));
                    }
                } else {
                    html.append(tp.translate("No properties defined."));
                }
            } else {
                html.append(tp.translate("Connection id provided."));
            }
        }
        catch (Exception e) {
            html.append(tp.translate("Error occurred:")).append(" ").append(e.getMessage());
        }
        return html.toString();
    }

    private void renderForm(StringBuffer html, String selected) {
        html.append("<form method=\"post\" id=\"frmRefresh\" name=\"frmRefresh\" action=\"rc?command=file&file=WEB-CORE/elements/richtext/collection.jsp\" target=\"\">");
        html.append("<input type=\"hidden\" name=\"properties\" id=\"properties\" value=\"{}\">");
        html.append("<input type=\"hidden\" name=\"selected\" id=\"selected\" value=\"").append(selected).append("\">");
        if (this.pageContext.getRequest().getParameter("iframe") != null) {
            html.append("<input type=\"hidden\" name=\"iframe\" id=\"iframe\" value=\"").append(this.pageContext.getRequest().getParameter("iframe")).append("\">");
        }
        if (this.pageContext.getRequest().getParameter("propertyid") != null) {
            html.append("<input type=\"hidden\" name=\"propertyid\" id=\"propertyid\" value=\"").append(this.pageContext.getRequest().getParameter("propertyid")).append("\">");
        }
        if (this.pageContext.getRequest().getParameter("callback") != null) {
            html.append("<input type=\"hidden\" name=\"callback\" id=\"callback\" value=\"").append(this.pageContext.getRequest().getParameter("callback")).append("\">");
        }
        if (this.pageContext.getRequest().getParameter("title") != null) {
            html.append("<input type=\"hidden\" name=\"title\" id=\"title\" value=\"").append(this.pageContext.getRequest().getParameter("title")).append("\">");
        }
        html.append("</form>");
    }

    private void renderScriptAndStyle(StringBuffer html) {
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/richtext/scripts/editcollection.js\">");
        html.append("</script>");
        html.append("<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/elements/richtext/stylesheets/formbuilder.css", this.pageContext) + "\" type=\"text/css\">");
        html.append("<style>");
        html.append("table.table_container{table-layout:fixed;width:99%;}");
        html.append("td{font-size:8pt;}");
        html.append("td.cell_right{border:solid 1px white;}");
        html.append("td.cell_left_point{background-color:white;width:18px;}");
        html.append("td.cell_left_item{border:solid 1px #7F9DB9;font-weight:bolder;color:black;background-image:URL(WEB-OPAL/layouts/images/tab_back.jpg);cursor: pointer;}");
        html.append("div.div_right{border:solid 1px #C3DAF9;width:100%;height:100%;}");
        html.append("div.div_right_inner{border:solid 1px #7F9DB9;width:100%;height:100%;}");
        html.append("</style>");
    }

    private void renderEndScript(StringBuffer html, PropertyList props, String prefix, String selected) {
        html.append("<script type=\"text/javascript\">");
        html.append("editCollection.properties = sapphire.util.propertyList.create(").append(props.toJSONString()).append(");");
        html.append("editCollection.collection = editCollection.properties.getCollection('collection');");
        html.append("editCollection.prefix = '").append(prefix).append("';");
        html.append("editCollection.selected = '").append(selected).append("';");
        if (this.elementid != null) {
            html.append("editCollection.elementId = '").append(this.elementid).append("';");
        }
        if (this.pageContext.getRequest().getParameter("propertyid") != null) {
            html.append("editCollection.propertyId = '").append(this.pageContext.getRequest().getParameter("propertyid")).append("';");
        }
        if (this.pageContext.getRequest().getParameter("callback") != null) {
            html.append("editCollection.callback = ").append(this.pageContext.getRequest().getParameter("callback")).append(";");
        }
        html.append("</script>");
    }

    private void renderButtons(StringBuffer html) {
        Button but = new Button(this.pageContext);
        but.setAction("editCollection.doAdd()");
        but.setTip(this.getTranslationProcessor().translate("Add a new item"));
        but.setImg("WEB-CORE/images/gif/Add.gif");
        html.append(but.getHtml());
        but = new Button(this.pageContext);
        but.setAction("editCollection.doMoveUp()");
        but.setTip(this.getTranslationProcessor().translate("Move item up"));
        but.setImg("WEB-CORE/images/gif/MoveUp.gif");
        html.append(but.getHtml());
        but = new Button(this.pageContext);
        but.setAction("editCollection.doMoveDown()");
        but.setTip(this.getTranslationProcessor().translate("Move item down"));
        but.setImg("WEB-CORE/images/gif/MoveDown.gif");
        html.append(but.getHtml());
        but = new Button(this.pageContext);
        but.setAction("editCollection.doRemove()");
        but.setTip(this.getTranslationProcessor().translate("Remove item"));
        but.setImg("WEB-CORE/images/gif/Delete.gif");
        html.append(but.getHtml());
        but = new Button(this.pageContext);
        but.setAction("editCollection.doCopy()");
        but.setTip(this.getTranslationProcessor().translate("Copy item"));
        but.setImg("WEB-CORE/images/gif/Copy.gif");
        html.append(but.getHtml());
    }

    private void renderItem(StringBuffer html, String selected, String label, String prefix, int index) {
        html.append("<tr>");
        html.append("<td class=\"cell_left_point\" align=\"left\" valign=\"center\">");
        if (selected.equals(prefix + "_div")) {
            html.append("<img id=\"").append(prefix).append("_div_img\" src=\"WEB-CORE/images/gif/Forward.gif\" style=\"display:block;\">");
        } else {
            html.append("<img id=\"").append(prefix).append("_div_img\" src=\"WEB-CORE/images/gif/Forward.gif\" style=\"display:none;\">");
        }
        html.append("</td>");
        html.append("<td class=\"cell_left_item\" align=\"left\" valign=\"center\" onclick=\"editCollection.itemClick('").append(prefix).append("');\">");
        html.append("<input type=\"checkbox\" id=\"").append(prefix).append("_div_check\" item=\"").append(index).append("\">");
        html.append("&nbsp;");
        html.append(label);
        html.append("</td>");
        html.append("</tr>");
    }

    private void renderInputs(StringBuffer html, PropertyList itemList, PropertyListCollection inputs, String visible, String prefix, int index) {
        String display = visible.equals(prefix + "_div") ? "block" : "none";
        html.append("<div id=\"").append(prefix).append("_div\" class=\"div_right\" style=\"display:").append(display).append(";\">");
        html.append("<div class=\"div_right_inner\">");
        TranslationProcessor tp = this.getTranslationProcessor();
        if (inputs != null && inputs.size() > 0) {
            html.append("<table border=0 cellpadding=0 cellspacing=0><tbody>");
            for (int k = 0; k < inputs.size(); ++k) {
                String value;
                PropertyList input = inputs.getPropertyList(k);
                String name = input.getProperty("name", "");
                if (name.length() <= 0) continue;
                String type = input.getProperty("type", "text");
                String label = input.getProperty("label", name);
                if (itemList.containsKey(name)) {
                    value = itemList.getProperty(name, input.getProperty("value", ""));
                } else {
                    value = input.getProperty("value", "");
                    itemList.setProperty(name, value);
                }
                html.append("<tr>");
                if (type.equalsIgnoreCase("select") || type.equalsIgnoreCase("editableselect") || type.equalsIgnoreCase("groovyselect") || type.equalsIgnoreCase("editablegroovyselect")) {
                    PropertyBuilder.renderListEditor(html, tp, name, prefix + '_' + name, label, input.getProperty("items", ""), value, false, type.contains("groovy"), type.contains("editable"), "editCollection.update(" + index + ", '" + name + "');", "editCollection.groovy.edit('" + prefix + "_" + name + "__EXPRESSION');", label, "");
                } else if (type.equalsIgnoreCase("sql") || type.equalsIgnoreCase("editablesql") || type.equalsIgnoreCase("groovysql")) {
                    String sql = input.getProperty("items", "");
                    if (sql.length() > 0) {
                        QueryProcessor qp = this.getQueryProcessor();
                        DataSet items = qp.getSqlDataSet(sql);
                        if (items != null && items.getColumnCount() > 0) {
                            int cols = items.getColumnCount();
                            StringBuffer vals = new StringBuffer();
                            for (int l = 0; l < items.getRowCount(); ++l) {
                                String itemcol = items.getColumnId(0);
                                String item = items.getValue(l, itemcol, "");
                                if (vals.length() > 0) {
                                    vals.append(";");
                                }
                                if (cols > 1) {
                                    String itemcol2 = items.getColumnId(1);
                                    String item2 = items.getValue(l, itemcol2, "");
                                    vals.append(item).append("=").append(item2);
                                    continue;
                                }
                                vals.append(item);
                            }
                            PropertyBuilder.renderListEditor(html, tp, name, prefix + '_' + name, label, vals.toString(), value, false, type.equalsIgnoreCase("groovysql"), type.equalsIgnoreCase("editablesql"), "editCollection.update(" + index + ", '" + name + "');", "", label, "");
                        } else {
                            html.append("<td>").append(tp.translate(label)).append("</td>");
                            html.append("<td>");
                            html.append(tp.translate("Failed to obtain data."));
                            html.append("</td>");
                        }
                    } else {
                        html.append("<td>").append(tp.translate(label)).append("</td>");
                        html.append("<td>");
                        html.append(tp.translate("No SQL provided."));
                        html.append("</td>");
                    }
                } else if (type.equalsIgnoreCase("lookup")) {
                    String[] items;
                    html.append("<td>").append(tp.translate(label)).append("</td>");
                    html.append("<td>");
                    html.append("<input onchange=\"editCollection.update(").append(index).append(", '").append(name).append("');\" type=\"").append(type).append("\" name=\"").append(prefix).append("_").append(name).append("\" id=\"").append(prefix).append("_").append(name).append("\" value=\"").append(value).append("\">");
                    PropertyList pdir = new PropertyList();
                    pdir.setProperty("rowsperpage", "50");
                    PropertyList lout = new PropertyList();
                    lout.setProperty("objectname", "WEB-OPAL/layouts/popup/popuplayout.jsp");
                    lout.setProperty("applicationtitle", "LabVantage");
                    lout.setProperty("hidetitle", "Y");
                    pdir.setProperty("layout", lout);
                    pdir.setProperty("sdcid", "");
                    pdir.setProperty("selectortype", "radiobutton");
                    PropertyListCollection buts = new PropertyListCollection();
                    PropertyList but = new PropertyList();
                    PropertyList cp = new PropertyList();
                    cp.setProperty("text", tp.translate("Select"));
                    cp.setProperty("tip", tp.translate("Select and Close"));
                    cp.setProperty("image", "WEB-CORE/images/gif/SelectAndReturn.gif");
                    but.setProperty("commonprops", cp);
                    PropertyList sp = new PropertyList();
                    sp.setProperty("action", "Accept");
                    but.setProperty("standardbuttonprops", sp);
                    buts.add(but);
                    but = new PropertyList();
                    cp = new PropertyList();
                    cp.setProperty("text", tp.translate("Cancel"));
                    cp.setProperty("tip", tp.translate("Cancel and Close"));
                    cp.setProperty("image", "WEB-CORE/images/gif/Cancel.gif");
                    but.setProperty("commonprops", cp);
                    sp = new PropertyList();
                    sp.setProperty("action", "Cancel");
                    but.setProperty("standardbuttonprops", sp);
                    buts.add(but);
                    pdir.setProperty("buttons", buts);
                    if (pdir != null) {
                        JSONObject jpdir = pdir.toJSONObject();
                        jpdir.remove("__propertylistid");
                        jpdir.remove("__propertylistsequence");
                        html.append("<script>");
                        html.append("oLUPD_").append(prefix).append("_").append(name).append("=").append(jpdir.toString()).append(";");
                        html.append("</script>");
                    }
                    if ((items = input.getProperty("items", "").split(";")).length > 0) {
                        String sdcid = items[0];
                        StringBuffer script = new StringBuffer();
                        if (sdcid.startsWith("[") && sdcid.endsWith("]")) {
                            String[] tok = StringUtil.getExpressionTokens(sdcid);
                            if (tok.length > 0) {
                                script.append("if(typeof(").append(tok[0]).append(")!='undefined' && ").append(tok[0]).append(".value.length>0){");
                                script.append("oLUPD_").append(prefix).append("_").append(name).append(".sdcid=").append(tok[0]).append(".value;");
                                script.append("}");
                            }
                        } else {
                            script.append("oLUPD_").append(prefix).append("_").append(name).append(".sdcid='").append(sdcid).append("';");
                        }
                        String fields = items.length > 1 ? items[1] : name;
                        String url = "rc?command=file&file=WEB-OPAL/pagetypes/list/maintenance_list.jsp";
                        script.append("if(oLUPD_").append(prefix).append("_").append(name).append(".sdcid.length>0){");
                        script.append("sapphire.lookup.sdi.open('").append(fields).append("',oLUPD_").append(prefix).append("_").append(name).append(".sdcid,'','N','','','','','','','','").append(url).append("','',true,oLUPD_").append(prefix).append("_").append(name).append(");");
                        script.append("}");
                        html.append("<input style=\"height:23px;\" type=\"button\" value=\"").append("...").append("\" onclick=\"").append(script).append("\">");
                    }
                    html.append("</td>");
                } else {
                    String onclick;
                    String change;
                    if (type.equalsIgnoreCase("groovytext")) {
                        change = "editCollection.groovy.doNormalChange(" + index + ", '" + name + "');";
                        onclick = "editCollection.groovy.edit('" + prefix + "_" + name + "__EXPRESSION')";
                    } else {
                        onclick = "";
                        change = "editCollection.update(" + index + ", '" + name + "');";
                    }
                    boolean disabled = false;
                    if (type.equalsIgnoreCase("readonly")) {
                        disabled = true;
                    }
                    PropertyBuilder.renderStringEditor(html, tp, name, prefix + '_' + name, label, value, disabled, false, change, "", onclick, false, label, "");
                }
                html.append("</tr>");
            }
            html.append("<tr>");
            html.append("<td colspan=2>");
            html.append("&nbsp;");
            html.append("</td>");
            html.append("</tr>");
        }
        html.append("</tbody></table>");
        html.append("</div>");
        html.append("</div>");
    }
}

