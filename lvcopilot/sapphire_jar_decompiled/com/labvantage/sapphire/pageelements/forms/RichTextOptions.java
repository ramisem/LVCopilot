/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.forms;

import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.pageelements.controls.TabGroup;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class RichTextOptions
extends BaseElement {
    public RichTextOptions(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    public RichTextOptions() {
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        try {
            String connectionId;
            String propstring = this.pageContext.getRequest().getParameter("properties");
            if (this.requestContext == null) {
                this.requestContext = RequestContext.getRequestContext(this.pageContext);
            }
            if ((connectionId = this.requestContext.getConnectionId()) == null || connectionId.length() == 0) {
                this.logger.debug("Passed connection id empty.");
            }
            if (propstring != null && propstring.length() > 0) {
                if (this.pageContext != null) {
                    this.logger.debug("Using page context for connection id");
                    connectionId = this.getConnectionId();
                    if (connectionId == null || connectionId.length() == 0) {
                        this.logger.debug("getConnection returned null or empty string. Trying request context.");
                        connectionId = RequestContext.getRequestContext(this.pageContext).getConnectionId();
                    }
                }
                if (connectionId != null && connectionId.length() > 0) {
                    TranslationProcessor tp = new TranslationProcessor(connectionId);
                    JSONObject job = new JSONObject(propstring);
                    PropertyList props = new PropertyList(job);
                    boolean selectfirst = props.getProperty("selectfirst", "N").equalsIgnoreCase("Y");
                    html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/richtext/scripts/options.js\">");
                    html.append("</script>");
                    html.append("<script type=\"text/javascript\">");
                    html.append("rtOptions.properties=sapphire.util.propertyList.create(").append(propstring).append(");");
                    html.append("rtOptions.selectfirst=").append(selectfirst).append(";");
                    html.append("</script>");
                    PropertyListCollection tabs = props.getCollection("tabs");
                    TabGroup tabgroup = new TabGroup();
                    QueryProcessor qp = new QueryProcessor(connectionId);
                    for (int i = 0; i < tabs.size(); ++i) {
                        PropertyList tab = tabs.getPropertyList(i);
                        String sText = tab.getProperty("text", "Tab " + i);
                        if (sText.length() <= 0) continue;
                        Tab tabel = new Tab();
                        tabel.setText(tp.translate(tab.getProperty("text", "Tab " + i)));
                        tabel.setBodyheight("100%");
                        tabel.setBodywidth("100%");
                        tabel.setExpandable("false");
                        tabel.setExpanded("true");
                        tabel.setId("tab" + i);
                        StringBuffer contents = new StringBuffer();
                        contents.append("<table border=0><tbody>");
                        PropertyListCollection inputs = tab.getCollection("inputs");
                        if (inputs != null && inputs.size() > 0) {
                            for (int k = 0; k < inputs.size(); ++k) {
                                PropertyList input = inputs.getPropertyList(k);
                                String name = input.getProperty("name", "");
                                if (name.length() <= 0) continue;
                                String type = input.getProperty("type", "text");
                                String label = input.getProperty("label", name);
                                String value = input.getProperty("value", "");
                                String onchange = input.getProperty("onchange", "").length() > 0 ? " onchange=\"" + input.getProperty("onchange", "") + "\" " : " ";
                                String onkeypress = input.getProperty("onkeypress", "").length() > 0 ? " onkeypress=\"" + input.getProperty("onkeypress", "") + "\" " : " ";
                                String onblur = input.getProperty("onblur", "").length() > 0 ? " onblur=\"" + input.getProperty("onblur", "") + "\" " : " ";
                                String onclick = input.getProperty("onclick", "").length() > 0 ? " onclick=\"" + input.getProperty("onclick", "") + "\" " : " ";
                                String onkeydown = input.getProperty("onkeydown", "").length() > 0 ? " onkeydown=\"" + input.getProperty("onkeydown", "") + "\" " : " ";
                                String onkeyup = input.getProperty("onkeyup", "").length() > 0 ? " onkeyup=\"" + input.getProperty("onkeyup", "") + "\" " : " ";
                                contents.append("<tr>");
                                if (type.equalsIgnoreCase("hidden")) {
                                    contents.append("<input type=\"").append(type).append("\" name=\"").append(name).append("\" id=\"").append(name).append("\" value=\"").append(value).append("\"");
                                    contents.append(">");
                                } else if (!type.equalsIgnoreCase("checkbox")) {
                                    contents.append("<td>").append(tp.translate(label)).append(":</td>");
                                }
                                if (type.equalsIgnoreCase("select")) {
                                    String[] items = input.getProperty("items", "").split(";");
                                    contents.append("<td>");
                                    contents.append("<select name=\"").append(name).append("\" id=\"").append(name).append("\" ").append(onchange).append(">");
                                    for (int l = 0; l < items.length; ++l) {
                                        if (items[l].equalsIgnoreCase(value)) {
                                            contents.append("<option value=\"").append(items[l]).append("\" SELECTED>").append(tp.translate(items[l])).append("</option>");
                                            continue;
                                        }
                                        contents.append("<option value=\"").append(items[l]).append("\">").append(tp.translate(items[l])).append("</option>");
                                    }
                                    contents.append("</select>");
                                    contents.append("</td>");
                                } else if (type.equalsIgnoreCase("sql")) {
                                    String sql = input.getProperty("items", "");
                                    if (sql.length() > 0) {
                                        DataSet items = qp.getSqlDataSet(sql);
                                        if (items != null && items.getColumnCount() > 0) {
                                            contents.append("<td>");
                                            contents.append("<select name=\"").append(name).append("\" id=\"").append(name).append("\" ").append(onchange).append(">");
                                            int cols = items.getColumnCount();
                                            for (int l = 0; l < items.getRowCount(); ++l) {
                                                String item2;
                                                String itemcol = items.getColumnId(0);
                                                String item = items.getValue(l, itemcol, "");
                                                if (cols > 1) {
                                                    String itemcol2 = items.getColumnId(1);
                                                    item2 = items.getValue(l, itemcol2, "");
                                                } else {
                                                    item2 = item;
                                                }
                                                if (item.equalsIgnoreCase(value)) {
                                                    contents.append("<option value=\"").append(item).append("\" SELECTED>").append(tp.translate(item2)).append("</option>");
                                                    continue;
                                                }
                                                contents.append("<option value=\"").append(item).append("\">").append(tp.translate(item2)).append("</option>");
                                            }
                                            if (value.length() == 0) {
                                                contents.append("<option value=\"").append("").append("\" SELECTED>").append("").append("</option>");
                                            } else {
                                                contents.append("<option value=\"").append("").append("\">").append("").append("</option>");
                                            }
                                            contents.append("</select>");
                                            contents.append("</td>");
                                        } else {
                                            contents.append("<td>");
                                            contents.append(tp.translate("Failed to obtain data."));
                                            contents.append("</td>");
                                        }
                                    } else {
                                        contents.append("<td>");
                                        contents.append(tp.translate("No SQL provided."));
                                        contents.append("</td>");
                                    }
                                } else if (type.equalsIgnoreCase("lookup")) {
                                    String[] items;
                                    contents.append("<td>");
                                    contents.append("<input type=\"").append(type).append("\" name=\"").append(name).append("\" id=\"").append(name).append("\" value=\"").append(value).append("\" ").append(onchange).append(">");
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
                                        contents.append("<script>");
                                        contents.append("oLUPD_").append(name).append("=").append(jpdir.toString()).append(";");
                                        contents.append("</script>");
                                    }
                                    if ((items = input.getProperty("items", "").split(";")).length > 0) {
                                        String sdcid = items[0];
                                        StringBuffer script = new StringBuffer();
                                        if (sdcid.startsWith("[") && sdcid.endsWith("]")) {
                                            String[] tok = StringUtil.getExpressionTokens(sdcid);
                                            if (tok.length > 0) {
                                                script.append("if(typeof(").append(tok[0]).append(")!='undefined' && ").append(tok[0]).append(".value.length>0){");
                                                script.append("oLUPD_").append(name).append(".sdcid=").append(tok[0]).append(".value;");
                                                script.append("}");
                                            }
                                        } else {
                                            script.append("oLUPD_").append(name).append(".sdcid='").append(sdcid).append("';");
                                        }
                                        String fields = items.length > 1 ? items[1] : name;
                                        String url = "rc?command=file&file=WEB-OPAL/pagetypes/list/maintenance_list.jsp";
                                        script.append("if(oLUPD_").append(name).append(".sdcid.length>0){");
                                        script.append("sapphire.lookup.sdi.open('").append(fields).append("',oLUPD_").append(name).append(".sdcid,'','N','','','','','','','','").append(url).append("','',true,oLUPD_").append(name).append(");");
                                        script.append("}");
                                        contents.append("<input style=\"height:23px;\" type=\"button\" value=\"").append("...").append("\" onclick=\"").append(script).append("\">");
                                    }
                                    contents.append("</td>");
                                } else if (!type.equalsIgnoreCase("hidden")) {
                                    StringBuffer script;
                                    if (type.equalsIgnoreCase("checkbox")) {
                                        contents.append("<td colspan='2'>");
                                        contents.append("<input type=\"").append(type).append("\" name=\"").append(name).append("\" id=\"").append(name).append("\" ");
                                        if (value.equalsIgnoreCase("y") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")) {
                                            contents.append(" checked ");
                                        }
                                        contents.append(onchange).append(">");
                                        contents.append(" ").append(tp.translate(label));
                                        contents.append("</td>");
                                    } else if (type.equalsIgnoreCase("image")) {
                                        script = new StringBuffer();
                                        script.append("sapphire.lookup.fileSystem.browseWeb('").append(name).append("','','").append(HttpUtil.getAppRoot(this.pageContext.getServletContext())).append("' );");
                                        contents.append("<td>");
                                        contents.append("<table cellpadding=0 cellspacing=0 border=0><tbody><tr><td>");
                                        contents.append("<img id=\"__").append(name).append("_img\" style=\"height:32px;width:32px\" src=\"").append(value).append("\">");
                                        contents.append("</td><td>");
                                        contents.append("<input type=\"").append("text").append("\" onchange=\"__").append(name).append("_img.src = this.value;\" name=\"").append(name).append("\" id=\"").append(name).append("\" value=\"").append(value).append("\"");
                                        contents.append(onchange).append(">");
                                        contents.append("</td><td>");
                                        contents.append("<input style=\"height:23px;\" type=\"button\" value=\"").append("...").append("\" onclick=\"").append(script).append("\">");
                                        contents.append("</td></tr></tbody></table>");
                                        contents.append("</td>");
                                    } else if (type.equalsIgnoreCase("url")) {
                                        script = new StringBuffer();
                                        script.append("sapphire.lookup.link.open('").append(name).append("');");
                                        contents.append("<td>");
                                        contents.append("<table cellpadding=0 cellspacing=0 border=0><tbody><tr><td>");
                                        contents.append("<input type=\"").append("text").append("\" name=\"").append(name).append("\" id=\"").append(name).append("\" value=\"").append(value).append("\"");
                                        contents.append(onchange).append(">");
                                        contents.append("</td><td>");
                                        contents.append("<input style=\"height:23px;\" type=\"button\" value=\"").append("...").append("\" onclick=\"").append(script).append("\">");
                                        contents.append("</td></tr></tbody></table>");
                                        contents.append("</td>");
                                    } else {
                                        contents.append("<td>");
                                        contents.append("<input type=\"").append(type).append("\" name=\"").append(name).append("\" id=\"").append(name).append("\" value=\"").append(value).append("\"");
                                        if (type.equalsIgnoreCase("readonly")) {
                                            contents.append(" readonly disabled ");
                                        }
                                        contents.append(onkeypress).append("");
                                        contents.append(onblur).append("");
                                        contents.append(onkeydown).append("");
                                        contents.append(onkeyup).append("");
                                        contents.append(onclick).append("");
                                        contents.append(onchange).append(">");
                                        contents.append("</td>");
                                    }
                                }
                                contents.append("</tr>");
                            }
                        }
                        contents.append("</tbody></table>");
                        if (contents.length() > 0) {
                            tabel.setContent(contents.toString());
                        }
                        tabgroup.setTab(tabel);
                    }
                    tabgroup.setId("tabgroup");
                    tabgroup.setBodyheight("100%");
                    tabgroup.setBodywidth("100%");
                    html.append(tabgroup.getHtml());
                } else {
                    html.append("No connection id could be obtained.");
                }
            } else {
                html.append("No properties defined or passed in.");
            }
        }
        catch (Exception e) {
            html.append("Error occurred: ").append(e.getMessage());
        }
        return html.toString();
    }
}

