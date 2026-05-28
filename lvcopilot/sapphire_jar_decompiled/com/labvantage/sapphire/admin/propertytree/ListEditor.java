/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.EditorUtil;
import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.servlet.RequestContext;
import sapphire.tagext.SDITagInfo;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.PropertyValue;

public class ListEditor
implements TypeSimple {
    /*
     * WARNING - void declaration
     */
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        int index;
        String currentValue;
        int i;
        StringBuffer output = new StringBuffer();
        ArrayList values = new ArrayList();
        ArrayList<String> displayvalues = new ArrayList<String>();
        String collectionproperty = (String)attributes.get("collectionproperty");
        String reftypeid = (String)attributes.get("reftypeid");
        String callbackMethod = (String)attributes.get("callbackmethod");
        String callbackArgs = attributes.containsKey("callbackarguments") ? (String)attributes.get("callbackarguments") : "";
        String valueslist = (String)attributes.get("values");
        String url = (String)attributes.get("url");
        String ddsql = (String)attributes.get("ddsql");
        String sdcid = (String)attributes.get("sdcid");
        String rootsdcid = "";
        if (sdcid == null && topPropertyList != null) {
            rootsdcid = topPropertyList.getProperty("sdcid");
        }
        String extendedwhere = (String)attributes.get("extendedwhere");
        String templatesOnly = (String)attributes.get("templatesonly");
        String format = (String)attributes.get("dateformat");
        String width = "200px";
        String initialExtraValue = (String)attributes.get("initialextravalues");
        if (initialExtraValue != null && initialExtraValue.length() > 0) {
            String[] svalues;
            for (String value : svalues = StringUtil.split(initialExtraValue, ";")) {
                int pos = value.indexOf("=");
                if (pos > -1) {
                    values.add(value.substring(0, pos));
                    displayvalues.add(value.substring(pos + 1).trim());
                    continue;
                }
                values.add(value);
                displayvalues.add(value);
            }
        }
        boolean isCallback = callbackMethod != null && callbackMethod.length() > 0;
        Button navigate = null;
        QueryProcessor qp = null;
        SDIProcessor sdiProcessor = null;
        if (pageContext != null) {
            qp = EditorUtil.getQueryProcessor(pageContext);
            sdiProcessor = EditorUtil.getSDIProcessor(pageContext);
        }
        if (url != null && url.length() > 0) {
            navigate = new Button(pageContext);
            navigate.setAction("var y='" + url + "'; var x = document.getElementById( '" + fieldName + "' ).value; x=x.replaceAll( '{|','');x=x.replaceAll( '|}',''); y=y.replaceAll( '[value]', x );window.open( y )");
            navigate.setImg("WEB-CORE/images/gif/Forward.gif");
            navigate.setMargin("none");
            navigate.setTip("Click this to navigate to edit this " + sdcid);
            navigate.setHighlight("false");
        }
        if (!isCallback) {
            int rowCount;
            DataSet ds;
            if (reftypeid != null && reftypeid.length() > 0 && qp != null) {
                ds = qp.getRefTypeDataSet(reftypeid);
                rowCount = ds.getRowCount();
                for (int i3 = 0; i3 < rowCount; ++i3) {
                    values.add(ds.getValue(i3, "refvalueid"));
                    displayvalues.add(ds.getValue(i3, "refdisplayvalue").length() > 0 ? ds.getValue(i3, "refdisplayvalue") : ds.getValue(i3, "refvalueid"));
                }
                navigate = new Button(pageContext);
                navigate.setAction("window.open( 'rc?command=page&page=RefTypeMaint&keyid1=" + reftypeid + "')");
                navigate.setImg("WEB-CORE/images/gif/Forward.gif");
                navigate.setMargin("none");
                navigate.setTip("Click this to navigate to edit this RefType");
                navigate.setHighlight("false");
            } else if (valueslist != null && valueslist.length() > 0) {
                String[] valuelist = StringUtil.split(valueslist, ";");
                for (int i2 = 0; i2 < valuelist.length; ++i2) {
                    String value = StringUtil.replaceAll(valuelist[i2].trim(), "#semicolon#", ";");
                    int pos = value.indexOf("=");
                    if (pos > -1) {
                        values.add(value.substring(0, pos));
                        displayvalues.add(value.substring(pos + 1).trim());
                        continue;
                    }
                    values.add(value);
                    displayvalues.add(value);
                }
            } else if (collectionproperty != null && collectionproperty.length() > 0) {
                String[] collectionlist = StringUtil.split(collectionproperty, ";");
                for (int i2 = 0; i2 < collectionlist.length; ++i2) {
                    collectionproperty = collectionlist[i2];
                    String[] parts = StringUtil.split(collectionproperty, "/");
                    if (parts.length != 2) continue;
                    PropertyList parentPropertyList = propertyValue.getParentPropertyList();
                    if (parentPropertyList != null && parentPropertyList.getProperty(parts[0]).length() == 0) {
                        PropertyList propertyList = parentPropertyList = parentPropertyList.getParentPropertyValue() != null ? parentPropertyList.getParentPropertyValue().getParentPropertyList() : null;
                    }
                    if (parentPropertyList != null && parentPropertyList.getProperty(parts[0]).length() == 0) {
                        PropertyList propertyList = parentPropertyList = parentPropertyList.getParentPropertyValue() != null ? parentPropertyList.getParentPropertyValue().getParentPropertyList() : null;
                    }
                    if (parentPropertyList != null && parentPropertyList.getProperty(parts[0]).length() == 0) {
                        PropertyList propertyList = parentPropertyList = parentPropertyList.getParentPropertyValue() != null ? parentPropertyList.getParentPropertyValue().getParentPropertyList() : null;
                    }
                    if (parentPropertyList != null && parentPropertyList.getProperty(parts[0]).length() == 0) {
                        PropertyList propertyList = parentPropertyList = parentPropertyList.getParentPropertyValue() != null ? parentPropertyList.getParentPropertyValue().getParentPropertyList() : null;
                    }
                    if (parentPropertyList == null) continue;
                    PropertyListCollection collection = parentPropertyList.getCollectionNotNull(parts[0]);
                    for (PropertyList propertyList : collection) {
                        if (values.contains(propertyList.getProperty(parts[1]))) continue;
                        values.add(propertyList.getProperty(parts[1]));
                        displayvalues.add(propertyList.getProperty(parts[1]));
                    }
                }
            } else if (ddsql != null && ddsql.length() > 0 && qp != null) {
                if (ddsql.contains("[sdcid]") && topPropertyList != null && topPropertyList.getProperty("sdcid").length() == 0) {
                    PropertyList parentPropertyList = propertyValue.getParentPropertyList();
                    if (parentPropertyList != null && parentPropertyList.getProperty("sdcid").length() == 0) {
                        PropertyList propertyList = parentPropertyList = parentPropertyList.getParentPropertyValue() != null ? parentPropertyList.getParentPropertyValue().getParentPropertyList() : null;
                    }
                    if (parentPropertyList != null) {
                        sdcid = parentPropertyList.getProperty("sdcid");
                    }
                    if (sdcid != null && sdcid.length() > 0) {
                        ddsql = StringUtil.replaceAll(ddsql, "[sdcid]", sdcid);
                    }
                }
                ddsql = EditorUtil.replaceTokens(ddsql, topPropertyList, propertyValue);
                ds = qp.getSqlDataSet(ddsql);
                rowCount = ds.getRowCount();
                String[] columns = ds.getColumns();
                if (columns.length > 1) {
                    displayvalues = new ArrayList<String>();
                }
                for (int i4 = 0; i4 < rowCount; ++i4) {
                    values.add(ds.getValue(i4, columns[0]));
                    if (displayvalues == null || columns.length <= 1) continue;
                    String dv = ds.getValue(i4, columns[1]);
                    if (dv != null && dv.length() > 0) {
                        displayvalues.add(dv);
                        continue;
                    }
                    displayvalues.add(ds.getValue(i4, columns[0]));
                }
            } else if (format != null && format.length() > 0) {
                ArrayList[] list = this.getDateFormatList(format);
                displayvalues = list[0];
                values = list[1];
                width = "200px";
            } else if (sdcid != null && sdcid.length() > 0 && !sdcid.equals("SDC") || rootsdcid != null && rootsdcid.length() > 0 && (extendedwhere != null && extendedwhere.length() > 0 || "Y".equalsIgnoreCase(templatesOnly))) {
                if ((sdcid == null || sdcid.length() == 0) && rootsdcid != null && rootsdcid.length() > 0) {
                    sdcid = rootsdcid;
                }
                SDCProcessor sdcproc = null;
                if (pageContext != null) {
                    sdcproc = EditorUtil.getSDCProcessor(pageContext);
                }
                if (sdcproc != null && qp != null) {
                    ArrayList<String> temp;
                    HashMap sdcprops = sdcproc.getSDCProperties(sdcid);
                    String keycolid1 = (String)sdcprops.get("keycolid1");
                    String tableid = (String)sdcprops.get("tableid");
                    if (extendedwhere != null && extendedwhere.contains("[sdcid]")) {
                        extendedwhere = EditorUtil.replaceToken(extendedwhere, "sdcid", propertyValue);
                    }
                    SDIRequest sdiRequest = new SDIRequest();
                    sdiRequest.setSDCid(sdcid);
                    sdiRequest.setQueryFrom(tableid);
                    if (extendedwhere != null && extendedwhere.length() > 0) {
                        sdiRequest.setQueryWhere(extendedwhere);
                    }
                    if (templatesOnly != null && templatesOnly.equalsIgnoreCase("Y")) {
                        sdiRequest.setShowTemplates("only");
                    }
                    sdiRequest.setRequestItem("primary");
                    String string = sdcid + ";" + tableid + ";" + extendedwhere + ";" + templatesOnly;
                    HashMap listEditorCache = (HashMap)pageContext.getAttribute("ListEditorCache");
                    if (listEditorCache == null) {
                        listEditorCache = new HashMap();
                        pageContext.setAttribute("ListEditorCache", listEditorCache);
                    }
                    if ((temp = (ArrayList<String>)listEditorCache.get(string)) == null) {
                        temp = new ArrayList<String>();
                        listEditorCache.put(string, temp);
                        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
                        DataSet ds2 = sdiData.getDataset("primary");
                        ds2.sort(ds2.getColumnId(0));
                        int rowCount2 = ds2.getRowCount();
                        for (i = 0; i < rowCount2; ++i) {
                            if (sdcid.equals("ParamList")) {
                                temp.add(ds2.getValue(i, "paramlistid") + "|" + ds2.getValue(i, "paramlistversionid") + "|" + ds2.getValue(i, "variantid"));
                                continue;
                            }
                            if (sdcid.equals("SpecSDC")) {
                                temp.add(ds2.getValue(i, "specid") + "|" + ds2.getValue(i, "specversionid"));
                                continue;
                            }
                            if (sdcid.equals("WorkItem")) {
                                temp.add(ds2.getValue(i, "workitemid") + "|" + ds2.getValue(i, "workitemversionid"));
                                continue;
                            }
                            temp.add(ds2.getValue(i, keycolid1));
                        }
                    }
                    values.addAll(temp);
                }
            } else if (sdcid != null && sdcid.equals("SDC") && qp != null) {
                String sql = "SELECT sdcid FROM sdc";
                if (extendedwhere != null && extendedwhere.length() > 0) {
                    sql = sql + " WHERE " + extendedwhere;
                }
                sql = sql + " ORDER BY sdcid";
                DataSet ds3 = qp.getSqlDataSet(sql);
                int rowCount3 = ds3.getRowCount();
                for (int i5 = 0; i5 < rowCount3; ++i5) {
                    values.add(ds3.getValue(i5, "sdcid"));
                }
            }
        }
        String disValue = currentValue = ancestorValue ? propertyValue.value : "";
        if (ancestorValue && displayvalues != null && values != null && values.size() == displayvalues.size() && currentValue.startsWith("{|") && currentValue.endsWith("|}") && (index = values.indexOf(currentValue.substring("{|".length(), currentValue.length() - "|}".length()))) >= 0) {
            disValue = "{|" + (String)displayvalues.get(index) + "|}";
        }
        StringBuffer options = new StringBuffer();
        String finalExtraValues = (String)attributes.get("finalextravalues");
        if (finalExtraValues != null && finalExtraValues.length() > 0) {
            String[] svalues;
            for (String value : svalues = StringUtil.split(finalExtraValues, ";")) {
                int pos = value.indexOf("=");
                if (pos > -1) {
                    values.add(value.substring(0, pos));
                    displayvalues.add(value.substring(pos + 1).trim());
                    continue;
                }
                values.add(value);
                displayvalues.add(value);
            }
        }
        int maxdisplayvaluelength = 150;
        String string = "";
        String customonchange = "";
        if (attributes.containsKey("customstyle")) {
            String string2 = attributes.get("customstyle").toString();
            if (!string2.endsWith(";")) {
                String string3 = string2 + ";";
            }
        } else {
            String string4 = "";
        }
        customonchange = attributes.containsKey("customonchange") ? attributes.get("customonchange").toString() : "propertyChange();";
        boolean editable = (attributes.containsKey("editable") ? attributes.get("editable").toString() : "N").equalsIgnoreCase("y");
        String editablemode = "";
        if (editable) {
            String string5 = editablemode = attributes.containsKey("editmode") ? attributes.get("editmode").toString() : "maintenance";
            if (editablemode.equalsIgnoreCase("maintenance") && pageContext == null) {
                editable = false;
                editablemode = "";
            }
        }
        boolean workItemSDC = "WorkItem".equals(sdcid);
        if (editablemode.length() == 0 || !editablemode.equalsIgnoreCase("maintenance")) {
            void var34_61;
            if (ancestorValue && values != null && currentValue.startsWith("{|") && currentValue.endsWith("|}")) {
                String actualValue = currentValue.substring("{|".length(), currentValue.length() - "|}".length());
                if (workItemSDC && !actualValue.contains("|")) {
                    boolean exists = false;
                    for (int w = 0; w < values.size(); ++w) {
                        if (!actualValue.equals(StringUtil.split((String)values.get(w), "|")[0])) continue;
                        exists = true;
                        break;
                    }
                    if (!exists) {
                        disValue = "?-" + disValue + "-?";
                    }
                } else if (!values.contains(actualValue)) {
                    disValue = "?-" + disValue + "-?";
                }
            }
            options.append("<option value=\"").append(ancestorValue ? propertyValue.value : "").append("\">").append(SafeHTML.encodeForHTML(disValue)).append("</option>");
            boolean selected = false;
            for (i = 0; i < values.size(); ++i) {
                String value;
                String displayvalue = value = (String)values.get(i);
                if (values.size() == displayvalues.size()) {
                    displayvalue = (String)displayvalues.get(i);
                }
                if (value != null && value.length() > 0) {
                    if (workItemSDC && !propertyValue.value.contains("|") && value.contains("|") && value.substring(0, value.indexOf("|")).equals(propertyValue.value)) {
                        selected = true;
                        options.append("<option value='").append(propertyValue.value).append("' selected >").append(propertyValue.value).append("</option>");
                        options.append("<option value='").append(value).append("'>").append(SafeHTML.encodeForHTML(displayvalue)).append("</option>");
                    } else if (value.equals(propertyValue.value)) {
                        selected = true;
                        options.append("<option value='").append(value).append("' selected>").append(SafeHTML.encodeForHTML(displayvalue)).append("</option>");
                    } else {
                        options.append("<option value='").append(value).append("'>").append(SafeHTML.encodeForHTML(displayvalue)).append("</option>");
                    }
                }
                if (displayvalue == null || displayvalue.length() < 20) continue;
                maxdisplayvaluelength = displayvalue.length() * 8;
            }
            if (!(ancestorValue || "".equals(propertyValue.value) || selected)) {
                if (propertyValue.value.startsWith("[variables.") && propertyValue.value.endsWith("]")) {
                    options.append("<option value='").append(propertyValue.value).append("' selected>").append(propertyValue.value).append("</option>");
                } else {
                    options.append("<option value='").append(propertyValue.value).append("' selected>").append("?-" + propertyValue.value + "-?").append("</option>");
                }
            }
            if (editablemode.equals("simple")) {
                RequestContext rc = pageContext != null ? RequestContext.getRequestContext(pageContext) : null;
                boolean html5 = rc != null ? rc.getProperty("html5").equalsIgnoreCase("Y") : false;
                Browser b = pageContext != null ? new Browser(pageContext) : null;
                output.append("<select name=\"").append(fieldName).append("_select\" id=\"").append(fieldName).append("_select\" style=\"").append(html5 ? "height:20px;" : "").append("width:").append(maxdisplayvaluelength).append("px; ").append(ancestorValue ? "color:blue;" : "").append((String)var34_61).append("\" onchange=\"this.style.color='black';").append("").append(fieldName).append(".value = this.value; sapphire.events.fireEvent( ").append(fieldName).append(", 'onblur');").append("\">");
                output.append(options);
                output.append("</select>");
                output.append("<table border=0 cellpadding=0 cellspacing=0 style=\"").append((String)var34_61).append("top:0;margin-top:-20px;margin-left:-18px;z-index:100;\"><tbody>");
                output.append("<tr><td style=\"width:18px;\">&nbsp;</td><td>");
                output.append("<input ").append("type=\"text\" ").append("name=\"").append(fieldName).append("\" ").append("id=\"").append(fieldName).append("\" ").append("style=\"width:").append(maxdisplayvaluelength - (html5 ? (b != null && b.isWebkit() ? 22 : 24) : 18)).append("px;").append((String)var34_61).append(ancestorValue ? "color:blue;" : "").append("\" ").append("onchange=\"this.style.color='black';checkEvent( this );\" ").append("onblur=\"").append(fieldName).append("_select.value = this.value;checkEvent( this );").append(customonchange).append("\" ").append("value=\"").append(propertyValue).append("\" ").append(attributes.containsKey("textentry") && attributes.get("textentry").toString().equalsIgnoreCase("N") ? "readonly" : "").append("/>");
                output.append("</td></tr>");
                output.append("</tbody></table>");
            } else {
                if (maxdisplayvaluelength > 150) {
                    output.append("<select name=\"").append(fieldName).append("\" id=\"").append(fieldName).append("\" style=\"").append(ancestorValue ? "color:blue;" : "").append((String)var34_61).append("\" onchange=\"this.style.color='black';checkEvent( this );").append(customonchange).append("\">");
                } else {
                    output.append("<select name=\"").append(fieldName).append("\" id=\"").append(fieldName).append("\" style=\"width:").append(maxdisplayvaluelength).append("px; ").append(ancestorValue ? "color:blue;" : "").append((String)var34_61).append("\" onchange=\"this.style.color='black';checkEvent( this );").append(customonchange).append("\">");
                }
                output.append(options);
                output.append("</select>");
            }
        } else if (pageContext != null) {
            output = new StringBuffer();
            PropertyList column = new PropertyList();
            column.setProperty("mode", "dropdowncombo");
            column.setProperty("columnid", fieldName);
            column.setProperty("name", fieldName);
            column.setProperty("value", propertyValue.value);
            column.setProperty("onchange", "this.style.color='black';checkEvent( this );" + customonchange);
            StringBuffer dropdownlist = new StringBuffer();
            for (int i6 = 0; i6 < values.size(); ++i6) {
                dropdownlist.append(i6 == 0 ? "" : ";").append(values.get(i6));
            }
            column.setProperty("dropdownvalues", dropdownlist.toString());
            column.setProperty("dropdowncomboid", fieldName);
            column.setProperty("dropdownid", fieldName);
            column.setProperty("style", ancestorValue ? "color:blue;" : "");
            column.setProperty("size", "30");
            output.append(SDITagUtil.getInstance(pageContext).getInputHtml(column, new SDITagInfo(new HashMap())));
            output.append("<script>").append(SDITagUtil.getInstance(pageContext).getValueListJSArray(column, new SDITagInfo(new HashMap()))).append("</script>");
        }
        String pastevalue = (String)attributes.get("pastevalue");
        if (pastevalue != null && pastevalue.startsWith("Y")) {
            output.append(EditorUtil.showPasteButton(fieldName, attributes, pageContext));
        }
        if (navigate != null) {
            navigate.setMargin("none");
            navigate.setStyle("height:16px");
            output.append("&nbsp;" + navigate.getHtml());
        }
        if (isCallback) {
            output.append("<script>addCallbackListItems('").append(fieldName).append("','").append(fieldName).append(editable && editablemode.equalsIgnoreCase("simple") ? "_select" : "").append("', '").append(callbackMethod + "', true, true);</script>");
        }
        return output.toString();
    }

    private ArrayList[] getDateFormatList(String format) {
        int i;
        ArrayList<String> values = new ArrayList<String>();
        ArrayList<String> displayvalues = new ArrayList<String>();
        int[] styles = new int[]{3, 2, 1};
        String[] display = new String[]{"S", "M", "L"};
        SimpleDateFormat sdf = null;
        int num = 3;
        if ("shortmedium".equals(format)) {
            num = 2;
        }
        for (i = 0; i < num; ++i) {
            sdf = (SimpleDateFormat)DateFormat.getDateInstance(styles[i]);
            values.add(display[i] + "........." + sdf.toLocalizedPattern());
            displayvalues.add(display[i]);
        }
        if (!"dateonly".equals(format)) {
            for (i = 0; i < num; ++i) {
                for (int j = 0; j < num; ++j) {
                    sdf = (SimpleDateFormat)DateFormat.getDateTimeInstance(styles[i], styles[j]);
                    values.add(display[i] + " " + display[j] + "........." + sdf.toLocalizedPattern());
                    displayvalues.add(display[i] + " " + display[j]);
                }
            }
        }
        ArrayList[] list = new ArrayList[]{values, displayvalues};
        return list;
    }
}

