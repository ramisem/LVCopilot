/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements;

import com.labvantage.opal.util.StorageUnitTypeDef;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.jsp.PageContext;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.tagext.SDITagInfo;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.JstlUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ElementUtil {
    private static HashMap lookuppageCache = new HashMap();

    public static String evaluateExpression(int row, String columnid, String expression, SDITagInfo sdiinfo) {
        return ElementUtil.evaluateExpression("primary", row, columnid, expression, sdiinfo, null);
    }

    public static String evaluateExpression(String datasetname, int row, String columnid, String expression, SDITagInfo sdiinfo, TranslationProcessor tp) {
        return ElementUtil.evaluateExpression(datasetname, row, columnid, expression, sdiinfo, tp, null);
    }

    public static String evaluateExpression(String datasetname, int row, String columnid, String expression, SDITagInfo sdiinfo, TranslationProcessor tp, PropertyList additionalProps) {
        return ElementUtil.evaluateExpression(datasetname, row, columnid, expression, sdiinfo, tp, additionalProps, false);
    }

    public static String evaluateExpression(String datasetname, int row, String columnid, String expression, SDITagInfo sdiinfo, TranslationProcessor tp, PropertyList additionalProps, boolean escapeHTML) {
        String[] tokens;
        boolean escSQ = false;
        String currentuser = "";
        if (expression.trim().toLowerCase().indexOf("javascript") == 0) {
            escSQ = true;
        }
        if ((tokens = StringUtil.getTokens(expression)) != null && tokens.length > 0) {
            if (row == -1) {
                row = sdiinfo.getCurrentRow(datasetname);
            }
            for (int i = 0; i < tokens.length; ++i) {
                if (tokens[i].equals("columnid") && columnid != null && columnid.length() > 0) {
                    String value = sdiinfo.getValue(datasetname, row, columnid);
                    if (tp != null) {
                        value = tp.translate(value);
                    }
                    expression = EncryptDecrypt.obfReplaceAll(expression, "[" + tokens[i] + "]", escSQ ? value.replaceAll("'", "\\'") : value);
                    continue;
                }
                if (tokens[i].startsWith("columnid=")) {
                    String value = sdiinfo.getValue(datasetname, row, tokens[i].substring(tokens[i].indexOf(61) + 1));
                    if (tp != null) {
                        value = tp.translate(value);
                    }
                    if (escapeHTML) {
                        value = SafeHTML.encodeForHTML(value, true);
                    }
                    expression = EncryptDecrypt.obfReplaceAll(expression, "[" + tokens[i] + "]", escSQ ? value.replaceAll("'", "\\'") : value);
                    continue;
                }
                if (tokens[i].equalsIgnoreCase("datasetkey")) {
                    if (datasetname.equals("dataset") || datasetname.equals("dataitem")) {
                        expression = StringUtil.replaceAll(expression, "[" + tokens[i] + "]", "sdcid=" + sdiinfo.getValue(datasetname, row, "sdcid") + "&keyid1=" + sdiinfo.getValue(datasetname, row, "keyid1") + "&paramlistid=" + sdiinfo.getValue(datasetname, row, "paramlistid") + "&paramlistversionid=" + sdiinfo.getValue(datasetname, row, "paramlistversionid") + "&variantid=" + sdiinfo.getValue(datasetname, row, "variantid") + "&dataset=" + sdiinfo.getValue(datasetname, row, "dataset"));
                        continue;
                    }
                    expression = EncryptDecrypt.obfReplaceAll(expression, "[" + tokens[i] + "]", "##Error: dataitemkey cannot be derived from current dataset");
                    continue;
                }
                if (tokens[i].equalsIgnoreCase("dataitemkey")) {
                    if (datasetname.equals("dataset") || datasetname.equals("dataitem")) {
                        expression = StringUtil.replaceAll(expression, "[" + tokens[i] + "]", "sdcid=" + sdiinfo.getValue(datasetname, row, "sdcid") + "&keyid1=" + sdiinfo.getValue(datasetname, row, "keyid1") + "&paramlistid=" + sdiinfo.getValue(datasetname, row, "paramlistid") + "&paramlistversionid=" + sdiinfo.getValue(datasetname, row, "paramlistversionid") + "&variantid=" + sdiinfo.getValue(datasetname, row, "variantid") + "&dataset=" + sdiinfo.getValue(datasetname, row, "dataset") + "&paramid=" + sdiinfo.getValue(datasetname, row, "paramid") + "&paramtype=" + sdiinfo.getValue(datasetname, row, "paramtype") + "&replicateid=" + sdiinfo.getValue(datasetname, row, "replicateid"));
                        continue;
                    }
                    expression = EncryptDecrypt.obfReplaceAll(expression, "[" + tokens[i] + "]", "##Error: dataitemkey cannot be derived from current dataset");
                    continue;
                }
                if (tokens[i].equalsIgnoreCase("rowid")) {
                    expression = EncryptDecrypt.obfReplaceAll(expression, "[" + tokens[i] + "]", sdiinfo.getRowId(datasetname));
                    continue;
                }
                if (tokens[i].equalsIgnoreCase("sdcid") || tokens[i].equalsIgnoreCase("sdc")) {
                    expression = EncryptDecrypt.obfReplaceAll(expression, "[" + tokens[i] + "]", tp == null ? sdiinfo.getSdcid() : tp.translate(sdiinfo.getSdcid()));
                    continue;
                }
                if (tokens[i].equalsIgnoreCase("keycolid1")) {
                    expression = EncryptDecrypt.obfReplaceAll(expression, "[" + tokens[i] + "]", tp == null ? sdiinfo.getValue(datasetname, row, sdiinfo.getKeycols()[0]) : tp.translate(sdiinfo.getValue(datasetname, row, sdiinfo.getKeycols()[0])));
                    continue;
                }
                if (tokens[i].equalsIgnoreCase("keycolid2")) {
                    if (sdiinfo.getKeycols().length <= 1) continue;
                    expression = EncryptDecrypt.obfReplaceAll(expression, "[" + tokens[i] + "]", tp == null ? sdiinfo.getValue(datasetname, row, sdiinfo.getKeycols()[1]) : tp.translate(sdiinfo.getValue(datasetname, row, sdiinfo.getKeycols()[1])));
                    continue;
                }
                if (tokens[i].equalsIgnoreCase("keycolid3")) {
                    if (sdiinfo.getKeycols().length <= 2) continue;
                    expression = EncryptDecrypt.obfReplaceAll(expression, "[" + tokens[i] + "]", tp == null ? sdiinfo.getValue(datasetname, row, sdiinfo.getKeycols()[2]) : tp.translate(sdiinfo.getValue(datasetname, row, sdiinfo.getKeycols()[2])));
                    continue;
                }
                if (tokens[i].equalsIgnoreCase("currentuser")) {
                    if (currentuser == null || currentuser.length() == 0 && sdiinfo.getPageContext() != null) {
                        currentuser = RequestContext.getRequestContext(sdiinfo.getPageContext()).getProperty("sysuserid");
                    }
                    expression = EncryptDecrypt.obfReplaceAll(expression, "[" + tokens[i] + "]", currentuser);
                    continue;
                }
                if (tokens[i].equalsIgnoreCase("storageunittypelabel")) {
                    String storageunitid = sdiinfo.getString("primary", row, "storageunitid");
                    String storageunittype = sdiinfo.getString("primary", row, "storageunittype");
                    PropertyList storageUnitTypeDef = storageunittype == null || storageunittype.length() == 0 ? StorageUnitTypeDef.getInstance().getTypeDefinitionByID(tp != null ? tp.getConnectionid() : null, storageunitid) : StorageUnitTypeDef.getInstance().getTypeDefinition(tp != null ? tp.getConnectionid() : null, storageunittype);
                    expression = EncryptDecrypt.obfReplaceAll(expression, "[" + tokens[i] + "]", storageUnitTypeDef.getProperty("storageunittypelabel", storageunittype));
                    expression = SafeHTML.encodeForHTML(expression, true);
                    continue;
                }
                if (sdiinfo.getDataSet(datasetname).isValidColumn(tokens[i]) && (row >= 0 || additionalProps == null)) {
                    String value = sdiinfo.getValue(datasetname, row, tokens[i]);
                    if (tp != null) {
                        value = tp.translate(value);
                    }
                    String string = value = escSQ ? sdiinfo.getValue(datasetname, row, tokens[i]).replaceAll("'", "\\\\'") : value;
                    if (escapeHTML) {
                        value = SafeHTML.encodeForHTML(value, true);
                    }
                    expression = EncryptDecrypt.obfReplaceAll(expression, "[" + tokens[i] + "]", value);
                    continue;
                }
                int temp = tokens[i].indexOf(".");
                if (temp > -1) {
                    String end;
                    String dsname = tokens[i].substring(0, temp);
                    DataSet ds = sdiinfo.getDataSet(dsname);
                    if (ds == null || (temp = (end = tokens[i].substring(temp + 1)).indexOf(".")) <= -1) continue;
                    try {
                        int srow = Integer.parseInt(end.substring(0, temp));
                        if (srow >= ds.getRowCount() || !ds.isValidColumn(end = end.substring(temp + 1))) continue;
                        String value = sdiinfo.getValue(dsname, srow, end);
                        if (tp != null) {
                            value = tp.translate(value);
                        }
                        value = escSQ ? sdiinfo.getValue(dsname, srow, end).replaceAll("'", "\\\\'") : value;
                        expression = EncryptDecrypt.obfReplaceAll(expression, "[" + tokens[i] + "]", value);
                    }
                    catch (Exception exception) {}
                    continue;
                }
                if (additionalProps == null || !additionalProps.containsKey(tokens[i])) continue;
                String value = additionalProps.getProperty(tokens[i], "");
                if (tp != null) {
                    value = tp.translate(value);
                }
                expression = EncryptDecrypt.obfReplaceAll(expression, "[" + tokens[i] + "]", escSQ ? value.replaceAll("'", "\\'") : value);
            }
        }
        return expression;
    }

    public static String evaluateExpression(String datasetname, int row, String columnid, String expression, SDITagInfo sdiinfo) {
        return ElementUtil.evaluateExpression(datasetname, row, columnid, expression, sdiinfo, null);
    }

    public static String getLink(String detailname, String columnid, SDITagInfo sdiInfo, PropertyList link) {
        return ElementUtil.getLink(detailname, columnid, sdiInfo, link, "", -1, null);
    }

    public static String parseUrl(String url, PropertyList props) {
        return ElementUtil.parseUrl(url, props, null, -1, null, null);
    }

    public static String parseUrl(String url, PropertyList props, String datasetname, int row, String columnid, SDITagInfo sdiinfo) {
        String out;
        String end;
        String start;
        boolean eval = datasetname != null && datasetname.length() > 0 && columnid != null && columnid.length() > 0 && sdiinfo != null;
        int queryindex = url.indexOf("?");
        if (queryindex > -1) {
            start = url.substring(0, queryindex);
            end = "?" + url.substring(queryindex + 1);
        } else {
            start = url;
            end = "";
        }
        url = (eval ? ElementUtil.evaluateExpression(datasetname, row, columnid, start, sdiinfo) : start) + end;
        queryindex = url.indexOf("?");
        try {
            if (queryindex > -1 && url.length() > queryindex + 1) {
                out = url.substring(0, queryindex);
                boolean keepCommand = out.endsWith("/rc") || out.length() == 2 && out.equals("rc");
                String querystring = url.substring(queryindex + 1);
                String[] params = querystring.split("&");
                StringBuffer outPlus = new StringBuffer();
                for (int i = 0; i < params.length; ++i) {
                    int index = params[i].indexOf("=");
                    if (index > -1) {
                        String value;
                        String name = eval ? ElementUtil.evaluateExpression(datasetname, row, columnid, params[i].substring(0, index), sdiinfo) : params[i].substring(0, index);
                        String string = value = eval ? ElementUtil.evaluateExpression(datasetname, row, columnid, params[i].substring(index + 1), sdiinfo) : params[i].substring(index + 1);
                        if (!keepCommand || !name.equalsIgnoreCase("command") && !name.equalsIgnoreCase("page") && !name.equalsIgnoreCase("file")) {
                            props.setProperty(name, value);
                            continue;
                        }
                        if (outPlus.length() > 0) {
                            outPlus.append("&");
                        }
                        outPlus.append(name).append("=").append(value);
                        continue;
                    }
                    props.setProperty(eval ? ElementUtil.evaluateExpression(datasetname, row, columnid, params[i], sdiinfo) : params[i], "");
                }
                if (outPlus.length() > 0) {
                    out = out + "?" + outPlus.toString();
                }
            } else {
                out = url;
            }
        }
        catch (Exception e) {
            out = url;
        }
        return out;
    }

    public static String getLink(String detailname, String columnid, SDITagInfo sdiInfo, PropertyList link, String linkdisplay, int currentRow, TranslationProcessor tp) {
        return ElementUtil.getLink(detailname, columnid, sdiInfo, link, linkdisplay, currentRow, true, tp);
    }

    public static String getLink(String detailname, String columnid, SDITagInfo sdiInfo, PropertyList link, String linkdisplay, int currentRow, boolean evaluateExpressions, TranslationProcessor tp) {
        return ElementUtil.getLink(detailname, columnid, sdiInfo, link, linkdisplay, currentRow, evaluateExpressions, tp, false);
    }

    public static String getLink(String detailname, String columnid, SDITagInfo sdiInfo, PropertyList link, String linkdisplay, int currentRow, boolean evaluateExpressions, TranslationProcessor tp, boolean useOnClick) {
        StringBuffer html = new StringBuffer();
        String target = link.getProperty("target");
        if (target == null || target.length() == 0) {
            target = "_self";
        }
        String href = link.getProperty("href");
        String twidth = link.getProperty("windowwidth", "");
        String theight = link.getProperty("windowheight", "");
        String tsd = link.getProperty("sapphiredialog", "");
        if ((target.equalsIgnoreCase("_blank") || target.equalsIgnoreCase("view") || twidth.length() > 0 || theight.length() > 0 || tsd.length() > 0) && !href.trim().toLowerCase().startsWith("javascript:")) {
            int height;
            int width;
            PropertyList props = new PropertyList();
            href = evaluateExpressions ? ElementUtil.parseUrl(href, props, detailname, currentRow, columnid, sdiInfo) : ElementUtil.parseUrl(href, props, null, currentRow, null, null);
            if (twidth.length() == 0) {
                twidth = "800";
            }
            if (theight.length() == 0) {
                theight = "600";
            }
            try {
                width = Integer.parseInt(twidth);
                height = Integer.parseInt(theight);
            }
            catch (Exception e) {
                width = 800;
                height = 600;
            }
            boolean sapphire = tsd.length() == 0 ? false : tsd.equalsIgnoreCase("Y");
            target = "_self";
            href = (useOnClick ? "" : "JavaScript:") + "if(typeof(sapphire)!='undefined')sapphire.lookup.util.openWindow('View','" + "LabVantage" + "','" + href + "'," + width + "," + height + "," + sapphire + "," + StringUtil.replaceAll(props.toJSONString(false), "\"", "&quot;") + ",false);";
        } else {
            if (evaluateExpressions) {
                href = ElementUtil.evaluateExpression(detailname, currentRow, columnid, href, sdiInfo);
            }
            if (useOnClick) {
                href = href.trim().toLowerCase().startsWith("javascript:") ? href.substring("javascript:".length()) : "sapphire.page.navigate( '" + href + "',null,'" + target + "' );";
            }
        }
        String title = link.getProperty("tip").trim();
        if (evaluateExpressions) {
            title = ElementUtil.evaluateExpression(detailname, currentRow, columnid, title, sdiInfo, tp);
        }
        if (href.toLowerCase().startsWith("javascript:")) {
            href = href + (href.endsWith(";") ? "" : ";") + "void(0);";
        }
        if (useOnClick) {
            html.append("<a href=\"#donothingxxx\" onClick=\"").append(href).append("\" ");
        } else {
            html.append("<a href=\"").append(href.replaceAll("%", "%25")).append("\" target=\"").append(target).append("\" ");
        }
        if (title != null && title.length() > 0) {
            html.append(" title=\"").append(title).append("\"");
        }
        if (linkdisplay.length() > 0) {
            html.append(">").append(linkdisplay).append("</a>");
        } else {
            String value = sdiInfo.getValue(detailname, columnid);
            if (value != null) {
                value = value.replaceAll("<", "&lt;");
            }
            html.append(">").append(value).append("</a>");
        }
        return html.toString();
    }

    public static void setRequest(String attributeName, String colExpr, PageContext pageContext) {
        StringBuffer sdiRequest;
        PropertyListCollection columns = (PropertyListCollection)JstlUtil.evaluateExpression(colExpr, pageContext);
        if (columns != null && columns.size() > 0) {
            sdiRequest = new StringBuffer("primary(" + columns.getPropertyList(0).getProperty("columnid"));
            for (int i = 1; i < columns.size(); ++i) {
                sdiRequest.append("+" + columns.getPropertyList(i).getProperty("columnid"));
            }
            sdiRequest.append(")");
        } else {
            sdiRequest = new StringBuffer("primary");
        }
        pageContext.setAttribute(attributeName, (Object)sdiRequest.toString());
    }

    public static void setTabProperties(Tab tab, PropertyList tabprops, String datasetname, TranslationProcessor tp) {
        String temp = tabprops.getProperty("id");
        if (temp != null && temp.length() > 0) {
            tab.setId(temp);
        } else {
            tab.setId(datasetname);
        }
        temp = tabprops.getProperty("text");
        if (temp != null && temp.length() > 0) {
            tab.setText(temp);
        } else {
            tab.setText(datasetname);
        }
        temp = tabprops.getProperty("collapsedText");
        if (temp != null && temp.length() > 0) {
            tab.setCollapsedtext(temp);
        } else {
            tab.setCollapsedtext(tp.translate("Click the tab to show more information."));
        }
        temp = tabprops.getProperty("width");
        if (temp != null && temp.length() > 0) {
            tab.setWidth(temp);
        }
        if ((temp = tabprops.getProperty("bodywidth")) != null && temp.length() > 0) {
            tab.setBodywidth(temp);
        } else {
            tab.setBodywidth("600");
        }
        temp = tabprops.getProperty("bodyheight");
        if (temp != null && temp.length() > 0) {
            tab.setBodyheight(temp);
        }
        if ((temp = tabprops.getProperty("expandable")) != null && temp.length() > 0) {
            tab.setExpandable(temp.equals("N") ? "false" : "true");
        } else {
            tab.setExpandable("true");
        }
        temp = tabprops.getProperty("expanded");
        if (temp != null && temp.length() > 0) {
            tab.setExpanded(temp.equals("Y") ? "true" : "false");
        } else {
            tab.setExpanded("false");
        }
        temp = tabprops.getProperty("appearance");
        if (temp != null && temp.length() > 0) {
            tab.setAppearance(temp);
        }
        if ((temp = tabprops.getProperty("highlight")) != null && temp.length() > 0) {
            tab.setHighlight(temp.equals("N") ? "false" : "true");
        }
        if ((temp = tabprops.getProperty("tip")) != null && temp.length() > 0) {
            tab.setTip(temp);
        }
    }

    public static void setButtonProperties(Button button, PropertyList buttonprops) {
        String temp = buttonprops.getProperty("id");
        if (temp != null && temp.length() > 0) {
            button.setId(temp);
        }
        if ((temp = buttonprops.getProperty("text")) != null && temp.length() > 0) {
            button.setText(temp);
        }
        if ((temp = buttonprops.getProperty("width")) != null && temp.length() > 0) {
            button.setWidth(temp);
        }
        if ((temp = buttonprops.getProperty("js")) != null && temp.length() > 0) {
            button.setAction(temp);
        }
        if ((temp = buttonprops.getProperty("img")) != null && temp.length() > 0) {
            button.setImg(temp);
        }
        if ((temp = buttonprops.getProperty("appearance")) != null && temp.length() > 0) {
            button.setAppearance(temp);
        }
        if ((temp = buttonprops.getProperty("margin")) != null && temp.length() > 0) {
            button.setMargin(temp);
        }
        if ((temp = buttonprops.getProperty("tip")) != null && temp.length() > 0) {
            button.setTip(temp);
        }
        if ((temp = buttonprops.getProperty("style")) != null && temp.length() > 0) {
            button.setStyle(temp);
        }
        if ((temp = buttonprops.getProperty("highlight")) != null && temp.length() > 0) {
            button.setHighlight(temp);
        }
    }

    public static void setSdcPropertyCache(PageContext pageContext, String connectionid, String sdcid, String attributeName) {
        SDCProcessor sdcProcessor;
        PropertyList sdc = (PropertyList)pageContext.getAttribute(attributeName);
        if ((sdc == null && sdcid.length() > 0 || sdc != null && !sdcid.equalsIgnoreCase(sdc.getProperty("sdcid"))) && (sdc = (sdcProcessor = new SDCProcessor(pageContext)).getPropertyList(sdcid)) != null) {
            pageContext.setAttribute(attributeName, (Object)sdc);
        }
    }

    public static String getText(PropertyList element, String textid, String defaulttext) {
        return ElementUtil.getText(element, textid, defaulttext, null);
    }

    public static String getText(PropertyList element, String textid, String defaulttext, TranslationProcessor tp) {
        PropertyList texts = element.getPropertyList("texts");
        if (tp != null) {
            defaulttext = tp.translate(defaulttext);
        }
        if (texts != null) {
            return texts.getProperty(textid, defaulttext);
        }
        return defaulttext;
    }

    public static String getDetailHtml(String[] primarykeyids, String[] detailcols, String datasetname, SDITagInfo sdiInfo, SDITagUtil sdiTagUtil, boolean addCurrentSequence, boolean dynamic) {
        StringBuffer html = new StringBuffer();
        SDIData sdiData = sdiInfo.getSDIData();
        QueryData queryData = sdiInfo.getQueryData(datasetname);
        DataSet data = queryData.getQuerydata();
        html.append(SDITagUtil.getFixedRowInputs(datasetname, data.getColumns(), data.size(), ""));
        html.append("<table border=\"1\" id=\"" + datasetname + "\">\n");
        for (int i = 0; i < primarykeyids.length; ++i) {
            html.append("<input type=\"text\" name=\"keyid" + (i + 1) + "\" id=\"keyid" + (i + 1) + "\" value=\"" + primarykeyids[i] + "\"/>");
        }
        PropertyList attributes = new PropertyList();
        attributes.setProperty("data", datasetname);
        attributes.setProperty("mode", "input");
        StringBuffer sequence = new StringBuffer();
        int i = 0;
        while (dynamic ? i <= data.size() : i < data.size()) {
            if (i == data.size()) {
                queryData.setTemplateGenerate();
                html.append("<tr id=\"__" + datasetname + "_templaterow\" style=\"display:block\"><td><table  style=\"display:block\" id=\"__" + datasetname + "_templatetable\">\n");
            } else {
                queryData.setCurrentRow(i);
                sequence.append(";" + sdiInfo.getRowId(datasetname));
            }
            html.append(SDITagUtil.getRepeatedRowInputs(datasetname, sdiData.getKeys(datasetname), queryData, "", "", 1));
            html.append("<tr id=\"__" + datasetname + sdiInfo.getRowId(datasetname) + "\">");
            html.append("<td rowid=\"__" + datasetname + sdiInfo.getRowId(datasetname) + "\">__" + datasetname + sdiInfo.getRowId(datasetname) + "</td>");
            html.append("<td>" + sdiInfo.getRowStatus(datasetname) + "</td>");
            for (int j = 0; detailcols != null && j < detailcols.length; ++j) {
                html.append("<td>");
                attributes.setProperty("columnid", detailcols[j]);
                attributes.setProperty("value", "");
                SDITagUtil.setIdentifierAttributes(attributes, sdiInfo);
                html.append(sdiTagUtil.getInputHtml(attributes, sdiInfo));
                html.append("</td>");
            }
            html.append("</tr>\n");
            if (i == data.size()) {
                html.append("</table></td></tr>\n");
            }
            ++i;
        }
        html.append("</table>");
        if (addCurrentSequence) {
            html.append("<script>__currentsequence[\"" + datasetname + "\"] = \"" + (sequence.length() > 0 ? sequence.substring(1) : "") + "\";</script>");
        }
        return html.toString();
    }

    public static void setColumnDateDisplayFormat(PageContext pageContext, PropertyListCollection columns, String datasetname, SDITagInfo sdiInfo) {
        ElementUtil.setColumnDateDisplayFormat(pageContext, columns, datasetname, sdiInfo, null);
    }

    public static void setColumnDateDisplayFormat(PageContext pageContext, PropertyListCollection columns, String datasetname, SDITagInfo sdiInfo, PropertyList sdcprops) {
        DataSet ds = sdiInfo.getDataSet(datasetname);
        for (int i = 0; i < columns.size(); ++i) {
            String columnid = ((PropertyList)columns.get(i)).getProperty("columnid");
            if (columnid.indexOf(" ") > 0) {
                columnid = RequestParser.parseAlias(columnid);
            }
            String formatStr = ((PropertyList)columns.get(i)).getProperty("format");
            if (columnid.length() <= 0 || ds.getColumnType(columnid) != 2) continue;
            boolean isDateOnly = false;
            if (sdcprops != null) {
                PropertyListCollection sdccolumns = sdcprops.getCollection("columns");
                PropertyList columnDef = sdccolumns.getPropertyList(columnid);
                if (columnDef != null && "Y".equals(columnDef.getProperty("timezoneindependent"))) {
                    isDateOnly = true;
                } else if (columnid.indexOf(".") > 0) {
                    PropertyListCollection links = sdcprops.getCollectionNotNull("links");
                    SDCProcessor sdcProcessor = null;
                    block1: for (int l = 0; l < links.size(); ++l) {
                        PropertyList link = links.getPropertyList(l);
                        if (columnid.indexOf(link.getProperty("sdccolumnid") + ".") != 0) continue;
                        if (sdcProcessor == null) {
                            sdcProcessor = new SDCProcessor(pageContext);
                        }
                        PropertyListCollection linksdccols = sdcProcessor.getColumns(link.getProperty("linksdcid"));
                        for (int c = 0; c < linksdccols.size(); ++c) {
                            if (!linksdccols.getPropertyList(c).getProperty("columnid").equals(columnid.substring(columnid.indexOf(".") + 1))) continue;
                            columnDef = linksdccols.getPropertyList(c);
                            if (columnDef == null || !"Y".equals(columnDef.getProperty("timezoneindependent"))) continue block1;
                            isDateOnly = true;
                            continue block1;
                        }
                    }
                }
            }
            if (formatStr.length() > 0) {
                if (isDateOnly) {
                    ds.setDateDisplayFormat(columnid, ElementUtil.getDateFormat(pageContext, formatStr, false));
                    ((PropertyList)columns.get(i)).setProperty("format", "O" + formatStr);
                    continue;
                }
                ds.setDateDisplayFormat(columnid, ElementUtil.getDateFormat(pageContext, formatStr));
                continue;
            }
            if (!isDateOnly) continue;
            ds.setTimeZoneInsensitive(columnid);
            ds.setDateDisplayFormat(columnid, new M18NUtil(pageContext).getDefaultDateOnlyFormat(false));
            ((PropertyList)columns.get(i)).setProperty("format", "O");
        }
    }

    public static void setColumnDisplayValue(PageContext pageContext, PropertyListCollection columns, PropertyList sdcprops, TranslationProcessor tp) {
        ElementUtil.setColumnDisplayValue(columns, sdcprops, tp, new QueryProcessor(pageContext), false);
    }

    public static void setColumnDisplayValue(PropertyListCollection columns, PropertyList sdcprops, TranslationProcessor tp, QueryProcessor queryProcessor, boolean displayIcon) {
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList columnLink;
            PropertyListCollection sdccolumns;
            PropertyList columnDef;
            boolean isDisplayIcon;
            String[] displays;
            PropertyList column = columns.getPropertyList(i);
            String columnid = column.getProperty("columnid");
            String mode = column.getProperty("mode");
            String reftypeid = column.getProperty("reftypeid");
            if (!"Display Icon".equalsIgnoreCase(mode) && !"displayicon".equalsIgnoreCase(mode) && !"Display Text".equals(mode) && !"".equals(mode) && mode.indexOf("Deferred Display") != 0) continue;
            String displaylist = column.getProperty("displayvalue");
            HashMap<String, String> displayValueMap = new HashMap<String, String>();
            if (displaylist != null && displaylist.length() > 0 && (displays = StringUtil.split(StringUtil.replaceAll(displaylist, "&lt;", "<"), ";")) != null && displays.length > 0) {
                for (int d = 0; d < displays.length; ++d) {
                    String display = displays[d].trim();
                    int pos = display.indexOf("=");
                    if (pos <= -1) continue;
                    String disval = display.substring(0, pos);
                    String displayValue = display.substring(pos + 1).trim();
                    displayValueMap.put(disval, displayValue);
                }
            }
            boolean bl = isDisplayIcon = "Display Icon".equals(mode) || "Deferred Display Icon".equals(mode) || "displayicon".equals(mode) || displayIcon;
            if (isDisplayIcon && reftypeid.length() == 0 && sdcprops != null && sdcprops != null && (columnDef = (sdccolumns = sdcprops.getCollection("columns")).getPropertyList(columnid)) != null && (columnLink = columnDef.getPropertyList("link")) != null) {
                String linksdcid = columnLink.getProperty("linksdcid", columnLink.getProperty("linksdcid", ""));
                String linktype = columnLink.getProperty("type", columnLink.getProperty("linktype", ""));
                if (linksdcid.equalsIgnoreCase("reftype") && !linktype.equalsIgnoreCase("F")) {
                    reftypeid = columnLink.getProperty("reftypeid", columnLink.getProperty("reftypeid", ""));
                }
            }
            if (reftypeid.length() > 0) {
                DataSet ds = queryProcessor.getRefTypeDataSet(reftypeid);
                for (int j = 0; j < ds.getRowCount(); ++j) {
                    String value;
                    String string = isDisplayIcon ? (ds.getValue(j, "refdisplayicon").length() > 0 ? ds.getValue(j, "refdisplayicon") : ds.getValue(j, "refdisplayvalue")) : (value = ds.getValue(j, "refdisplayvalue").length() > 0 ? ds.getValue(j, "refdisplayvalue") : ds.getValue(j, "refvalueid"));
                    if (isDisplayIcon && ds.getValue(j, "refdisplayicon").length() > 0 && value.trim().indexOf("<") != 0) {
                        String title;
                        String href = column.getPropertyList("link") != null && column.getPropertyList("link").getProperty("href").trim().length() > 0 ? column.getPropertyList("link").getProperty("href").trim() : "";
                        String tip = column.getProperty("tip");
                        String string2 = tip.length() > 0 ? tip : (title = ds.getValue(j, "refvaluedesc").length() > 0 ? ds.getValue(j, "refvaluedesc") : ds.getValue(j, "refdisplayvalue"));
                        value = href.length() > 0 && "displayicon".equalsIgnoreCase(mode) ? "<a href=\"#donothingxxx\" onclick=\"" + StringUtil.replaceAll(StringUtil.replaceAll(href, "\"", "&quot;"), ";", "#semicolon#") + "\"><img src=\"" + value + "\" title=\"" + title + "\"/></a>" : "<img src=\"" + value + "\" title=\"" + title + "\"/>";
                    } else {
                        value = tp.translatePartial(value);
                    }
                    displayValueMap.put(ds.getValue(j, "refvalueid"), value);
                }
            }
            Iterator keysetItr = displayValueMap.keySet().iterator();
            StringBuilder stringBuilder = new StringBuilder();
            while (keysetItr.hasNext()) {
                String value = (String)keysetItr.next();
                stringBuilder.append(";" + value + "=" + (String)displayValueMap.get(value));
            }
            column.setProperty("displayvalue", stringBuilder.indexOf(";") == 0 ? stringBuilder.substring(1) : "");
        }
    }

    public static void setColumnDefaultTitle(PropertyListCollection columns, PropertyListCollection ddtcolumns, TranslationProcessor translationProcessor) {
        if (columns != null && ddtcolumns != null) {
            String columnid;
            PropertyList column;
            int i;
            HashMap<String, String> idLabelMap = new HashMap<String, String>();
            for (i = 0; i < ddtcolumns.size(); ++i) {
                column = ddtcolumns.getPropertyList(i);
                columnid = column.getProperty("columnid");
                String columnlabel = column.getProperty("columnlabel");
                idLabelMap.put(columnid, columnlabel);
            }
            for (i = 0; i < columns.size(); ++i) {
                String columnlabel;
                column = columns.getPropertyList(i);
                columnid = column.getProperty("columnid");
                String title = column.getProperty("title");
                if (title.length() != 0 || columnid.indexOf(" ") >= 0 || (columnlabel = (String)idLabelMap.get(columnid)) == null || columnlabel.length() <= 0) continue;
                title = translationProcessor.translate(columnlabel);
                column.setProperty("title", title);
            }
        }
    }

    public static DateFormat getDateFormat(PageContext pageContext, String formatStr) {
        return ElementUtil.getDateFormat(pageContext, formatStr, true);
    }

    public static DateFormat[] getDateFormattors(PageContext pageContext, String formatStr, String datepartformat, String timepartformat, boolean isTimeZoneAware) {
        DateFormat[] dateFormats = new DateFormat[3];
        M18NUtil m18n = new M18NUtil(pageContext);
        Locale locale = m18n.getLocale();
        TimeZone timeZone = m18n.getTimezone();
        if (formatStr != null && formatStr.trim().length() > 3) {
            dateFormats[0] = m18n.getDefaultDateFormat();
            if (datepartformat.length() == 0 || timepartformat.length() == 0) {
                int hourIndex;
                int n = hourIndex = formatStr.indexOf("H") > 0 ? formatStr.indexOf("H") : formatStr.indexOf("h");
                if (hourIndex > 0) {
                    datepartformat = datepartformat.length() == 0 ? formatStr.substring(0, hourIndex).trim() : datepartformat;
                    timepartformat = timepartformat.length() == 0 ? formatStr.substring(hourIndex) : timepartformat;
                }
            }
            dateFormats[1] = datepartformat.length() > 0 ? new SimpleDateFormat(datepartformat, locale) : m18n.getDefaultDateOnlyFormat(isTimeZoneAware);
            dateFormats[2] = timepartformat.length() > 0 ? new SimpleDateFormat(timepartformat, locale) : DateFormat.getTimeInstance();
            if (isTimeZoneAware) {
                dateFormats[1].setTimeZone(timeZone);
                dateFormats[2].setTimeZone(timeZone);
            }
        } else {
            String[] formatS;
            if (formatStr == null || formatStr.length() == 0) {
                formatStr = "S S";
            }
            int dformat = (formatS = StringUtil.split(formatStr, " "))[0].equals("S") ? 3 : (formatS[0].equals("L") ? 1 : 2);
            DateFormat dateformat = null;
            if (formatS.length == 2) {
                int tformat = formatS[1].equals("S") ? 3 : (formatS[1].equals("L") ? 1 : 2);
                dateformat = DateFormat.getDateTimeInstance(dformat, tformat, locale);
                dateFormats[1] = DateFormat.getDateInstance(dformat, locale);
                dateFormats[2] = DateFormat.getTimeInstance(tformat, locale);
                if (isTimeZoneAware) {
                    dateFormats[1].setTimeZone(timeZone);
                    dateFormats[2].setTimeZone(timeZone);
                }
            } else {
                dateformat = formatS.length == 1 ? DateFormat.getDateInstance(dformat, locale) : m18n.getDefaultDateOnlyFormat();
            }
            dateFormats[0] = dateformat;
            if (isTimeZoneAware) {
                dateFormats[0].setTimeZone(I18nUtil.getSessionTimeZone(pageContext));
            }
        }
        return dateFormats;
    }

    public static DateFormat getDateFormat(PageContext pageContext, String formatStr, boolean isTimeZoneAware) {
        return ElementUtil.getDateFormat(formatStr, isTimeZoneAware, new M18NUtil(pageContext), I18nUtil.getSessionTimeZone(pageContext));
    }

    public static DateFormat getDateFormat(String formatStr, boolean isTimeZoneAware, M18NUtil m18n, TimeZone timeZone) {
        String[] formatS = StringUtil.split(formatStr, " ");
        int dformat = formatS[0].equals("S") ? 3 : (formatS[0].equals("L") ? 1 : 2);
        DateFormat dateformat = null;
        Locale locale = m18n.getLocale();
        if (formatS.length == 2) {
            int tformat = formatS[1].equals("S") ? 3 : (formatS[1].equals("L") ? 1 : 2);
            dateformat = DateFormat.getDateTimeInstance(dformat, tformat, locale);
        } else {
            dateformat = formatS.length == 1 ? DateFormat.getDateInstance(dformat, locale) : m18n.getDefaultDateOnlyFormat();
        }
        if (isTimeZoneAware) {
            dateformat.setTimeZone(timeZone);
        }
        return dateformat;
    }

    public static DateFormat getDateFormat(ConnectionInfo connectionInfo, String formatStr) {
        return ElementUtil.getDateFormat(connectionInfo, formatStr, true);
    }

    public static DateFormat getDateFormat(ConnectionInfo connectionInfo, String formatStr, boolean isTimeZoneAware) {
        String[] formatS = StringUtil.split(formatStr, " ");
        int dformat = formatS[0].equals("S") ? 3 : (formatS[0].equals("L") ? 1 : 2);
        DateFormat dateformat = null;
        M18NUtil m18n = new M18NUtil(connectionInfo);
        Locale locale = m18n.getLocale();
        if (formatS.length == 2) {
            int tformat = formatS[1].equals("S") ? 3 : (formatS[1].equals("L") ? 1 : 2);
            dateformat = DateFormat.getDateTimeInstance(dformat, tformat, locale);
        } else {
            dateformat = formatS.length == 1 ? DateFormat.getDateInstance(dformat, locale) : m18n.getDefaultDateOnlyFormat();
        }
        if (isTimeZoneAware) {
            dateformat.setTimeZone(I18nUtil.getConnectionTimeZone(connectionInfo));
        }
        return dateformat;
    }

    public static String getSDCLookUpPage(String sdcid, QueryProcessor qp) {
        String lookuppageid = "";
        String database = SecurityService.getDatabaseId(qp.getConnectionid());
        if (lookuppageCache.get(database) == null) {
            DataSet ds = qp.getSqlDataSet("SELECT webpageid from webpage where webpageid like '%Lookup'");
            HashMap<String, String> pagemap = new HashMap<String, String>();
            for (int i = 0; i < ds.getRowCount(); ++i) {
                pagemap.put(ds.getString(i, "webpageid"), ds.getString(i, "webpageid"));
            }
            lookuppageCache.put(database, pagemap);
        }
        String string = ((HashMap)lookuppageCache.get(database)).get(sdcid + "Lookup") != null ? (String)((HashMap)lookuppageCache.get(database)).get(sdcid + "Lookup") : (lookuppageid = ((HashMap)lookuppageCache.get(database)).get("LV_" + sdcid + "Lookup") != null ? (String)((HashMap)lookuppageCache.get(database)).get("LV_" + sdcid + "Lookup") : "");
        if ("WorkItem".equals(sdcid) && (lookuppageid == null || lookuppageid.length() == 0)) {
            lookuppageid = "LV_WorkitemIDLookupSingle";
        }
        return lookuppageid;
    }

    public static String evaluatePageDirectives(String expression, SDITagInfo sdiinfo) {
        boolean escPageDir = false;
        String currentuser = "";
        if (expression.trim().toLowerCase().indexOf("javascript") > 0) {
            escPageDir = true;
        }
        if (escPageDir) {
            expression = ElementUtil.appendPageDirId(expression, sdiinfo);
        }
        return expression;
    }

    private static String appendPageDirId(String expression, SDITagInfo sdiinfo) {
        int startindex = expression.trim().toLowerCase().indexOf("<script>");
        int endindex = expression.trim().toLowerCase().indexOf("</script>");
        if (startindex > 0 && endindex > 0) {
            String scriptText = expression.substring(startindex + 8, endindex);
            String scriptVar = scriptText.substring(scriptText.indexOf("{"), scriptText.lastIndexOf("}") + 1);
            try {
                PropertyList props = new PropertyList(new JSONObject(scriptVar));
                String id = SDITagUtil.generateId(props.getProperty("sdcid", "pageDirectives"), "PGD_", -1);
                props.setProperty("pageDirId", id);
                JSONObject job = props.toJSONObject(false);
                job.remove("__propertylistid");
                job.remove("__propertylistsequence");
                sdiinfo.getPageContext().getSession().setAttribute(id, (Object)job.toString());
                expression = expression.replace(scriptVar, job.toString());
            }
            catch (JSONException jSONException) {
                // empty catch block
            }
        }
        return expression;
    }
}

