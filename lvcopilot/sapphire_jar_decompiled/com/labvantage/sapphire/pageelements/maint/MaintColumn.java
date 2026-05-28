/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.tagext.SDITagUtil;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.tagext.SDITagInfo;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MaintColumn
extends BaseElement {
    private String id = "";
    private PropertyList sdc;
    private HashMap<String, PropertyListCollection> linkcolumns;
    private String rowStatus = "";
    private String datasetname = "primary";
    private PropertyList column;
    private static int bytesperchar = 0;
    private PropertyList inputAttributes = null;
    private PropertyList columnDefinition = null;
    private HashMap<String, String> columnDependsMap;
    boolean isLocked = false;

    public MaintColumn(PageContext pageContext, SDITagInfo sdiInfo, String connectionid) {
        this.browser = new Browser(pageContext);
        this.pageContext = pageContext;
        this.requestContext = (RequestContext)pageContext.getRequest().getAttribute("RequestContext");
        this.connectionInfo = new ConnectionProcessor(pageContext).getConnectionInfo(this.getConnectionid());
        this.sdiInfo = sdiInfo;
        this.setConnectionId(connectionid);
        if (bytesperchar == 0) {
            try {
                bytesperchar = Integer.parseInt(System.getProperty("sapphire.bytesperchar." + connectionid.substring(0, connectionid.indexOf("|"))));
            }
            catch (Throwable t) {
                bytesperchar = 1;
            }
        }
        if (bytesperchar < 1 || bytesperchar > 4) {
            bytesperchar = 1;
        }
    }

    public void setColumnDependsMap(HashMap<String, String> columnDependsMap) {
        this.columnDependsMap = columnDependsMap;
    }

    protected PropertyList getColumn() {
        return this.column;
    }

    protected PropertyList getColumnDefinition() {
        return this.columnDefinition;
    }

    protected void setColumnDefinition(PropertyList colDef) {
        this.columnDefinition = colDef;
    }

    public void setColumn(PropertyList column) {
        this.column = column;
    }

    public void setColumnProperty(String propertyid, String value) {
        if (this.column != null) {
            this.column.setProperty(propertyid, value);
        }
    }

    public void setRowStatus(String status) {
        this.rowStatus = status;
    }

    public void setLocked(boolean locked) {
        this.isLocked = locked;
    }

    public void setDatasetname(String datasetname) {
        this.datasetname = datasetname;
    }

    protected String getDatasetname() {
        return this.datasetname;
    }

    public void setSdcPropertyList(PropertyList sdc) {
        this.sdc = sdc;
    }

    public void setInputAttributes(PropertyList attributes) {
        this.inputAttributes = attributes;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer(this.prefix != null ? this.prefix : "");
        PropertyList attributes = this.inputAttributes;
        if (attributes == null) {
            attributes = this.getInputAttributes();
        }
        attributes.setProperty("data", this.datasetname);
        if ("prompt".equals(this.element.getProperty("mode"))) {
            attributes.setProperty("name", this.column.getProperty("columnid"));
            attributes.setProperty("value", this.column.getProperty("defaultvalue"));
        }
        if (attributes.getProperty("name").length() == 0) {
            SDITagUtil.setIdentifierAttributes(attributes, this.sdiInfo);
        } else if (attributes.getProperty("mode").equalsIgnoreCase("datelookup") && attributes.getProperty("dateformat") == "") {
            try {
                attributes.setProperty("dateformat", this.getConfigurationProcessor().getPolicy("DateFormatPolicy", "Sapphire Custom").getProperty("defaultdateformat", ""));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        String compcode = Configuration.getCompcode(this.connectionInfo.getDatabaseId());
        if (this.rowStatus.equals("I") && compcode.length() > 0 && this.sdc.getProperty("componentableflag").equals("Y") && this.sdc.getProperty("keycolid1").equals(this.column.getProperty("columnid"))) {
            html.append(compcode).append("_");
            try {
                int maxlen = Integer.parseInt(attributes.getProperty("maxlen")) - 4;
                attributes.setProperty("maxlen", String.valueOf(maxlen));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (this.rowStatus.equals("I") || "prompt".equals(this.element.getProperty("mode"))) {
            attributes.setProperty("value", this.evalGroovyProperty(attributes.getProperty("value")));
        }
        if ("Y".equals(this.column.getProperty("userelativedateformat")) && "readonly".equals(this.column.getProperty("mode"))) {
            attributes.put("calendar", this.sdiInfo.getCalendar(this.datasetname, this.column.getProperty("columnid")));
            attributes.setProperty("userelativedateformat", "Y");
        }
        html.append(SDITagUtil.getInstance(this.pageContext).getInputHtml(attributes, this.sdiInfo));
        this.id = attributes.getProperty("name");
        this.sdiInfo.getQueryData(this.datasetname).addGridColumn(this.id);
        if (attributes.getProperty("mode").equals("dropdowncombo")) {
            SDITagUtil.getInstance(this.pageContext).collectDropDownComboInfo(attributes, this.sdiInfo, this.pageContext);
        }
        html.append(this.suffix != null ? this.suffix : "");
        return html.toString();
    }

    public void setInputAttributes(String var) {
        this.pageContext.setAttribute(var, (Object)this.getInputAttributes());
    }

    protected PropertyList getAttributes() {
        return this.inputAttributes;
    }

    protected void setAttributes(PropertyList attributes) {
        this.inputAttributes = attributes;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public PropertyList getInputAttributes() {
        String disabled;
        PropertyList attributes;
        block139: {
            String mode;
            block157: {
                boolean isLocked;
                block138: {
                    block158: {
                        block160: {
                            block159: {
                                boolean isPageViewOnly;
                                block149: {
                                    block155: {
                                        String columnid;
                                        block150: {
                                            boolean detail;
                                            block156: {
                                                String pkFlag;
                                                block137: {
                                                    String datatype;
                                                    block154: {
                                                        TranslationProcessor tp;
                                                        block151: {
                                                            block153: {
                                                                block152: {
                                                                    PropertyList columnLink;
                                                                    String length;
                                                                    PropertyList columnDef;
                                                                    boolean forcePseudo;
                                                                    block148: {
                                                                        block136: {
                                                                            String columnt;
                                                                            block143: {
                                                                                String[] parts;
                                                                                block147: {
                                                                                    PropertyList link;
                                                                                    boolean fkcolumn;
                                                                                    block146: {
                                                                                        String linkkey;
                                                                                        block144: {
                                                                                            block145: {
                                                                                                String colalias;
                                                                                                boolean sdialias;
                                                                                                block142: {
                                                                                                    block140: {
                                                                                                        block141: {
                                                                                                            PropertyListCollection sdccolumns;
                                                                                                            PropertyList dynamicdropdown;
                                                                                                            String initialbehavior;
                                                                                                            String tip;
                                                                                                            PropertyList link2;
                                                                                                            tp = this.getTranslationProcessor();
                                                                                                            attributes = new PropertyList();
                                                                                                            if (this.column == null) break block139;
                                                                                                            attributes.setProperty("_prefix", this.element.getProperty("_prefix"));
                                                                                                            mode = this.column.getProperty("mode");
                                                                                                            columnid = this.column.getProperty("columnid");
                                                                                                            if (mode.equals("default")) {
                                                                                                                this.column.setProperty("mode", "");
                                                                                                                mode = "";
                                                                                                            }
                                                                                                            sdialias = columnid.toLowerCase().startsWith("sdialias.");
                                                                                                            if (!(this.sdiInfo != null && this.sdiInfo.getSdcid() != null && sdialias || columnid.indexOf(" ") <= 0)) {
                                                                                                                columnid = RequestParser.parseAlias(columnid);
                                                                                                            }
                                                                                                            if (columnid.length() == 0) {
                                                                                                                columnid = "(UNDEFINED)";
                                                                                                            }
                                                                                                            attributes.setId(this.column.getProperty("id", ""));
                                                                                                            attributes.setProperty("columnid", columnid);
                                                                                                            forcePseudo = false;
                                                                                                            if (mode.equals("readonly") && this.element.containsKey("style") && this.element.getProperty("style").toLowerCase().contains("grid") && (link2 = this.column.getPropertyList("link")) != null && link2.getProperty("href").length() > 0 && this.element.getProperty("pseudocolumn").length() == 0) {
                                                                                                                attributes.setProperty("datasetname", this.datasetname);
                                                                                                                attributes.setProperty("mode", "hidden");
                                                                                                                attributes.setProperty("columnid", columnid);
                                                                                                                this.column.setProperty("pseudocolumn", "<input name=\"[name]\" id=\"[name]\" type=\"hidden\" value=\"[" + columnid + "]\"/>" + ElementUtil.getLink(this.datasetname, columnid, this.sdiInfo, link2, "[" + columnid + "]", this.sdiInfo.getCurrentRow(), false, "Y".equals(this.column.getProperty("translatevalue")) ? this.getTranslationProcessor() : null, true));
                                                                                                                forcePseudo = true;
                                                                                                            }
                                                                                                            if ((tip = this.column.getProperty("tip")).length() > 0) {
                                                                                                                attributes.setProperty("tip", ElementUtil.evaluateExpression(this.datasetname, -1, columnid, tip, this.sdiInfo));
                                                                                                            }
                                                                                                            attributes.setProperty("direction", this.column.getProperty("direction"));
                                                                                                            attributes.setProperty("title", this.evalGroovyProperty(this.column.getProperty("title")));
                                                                                                            PropertyList dynamicrenderingPL = this.column.getPropertyList("dynamicrendering");
                                                                                                            if (dynamicrenderingPL != null && (initialbehavior = this.evalGroovyProperty(dynamicrenderingPL.getProperty("hiddenif")) + ";" + this.evalGroovyProperty(dynamicrenderingPL.getProperty("mandatoryif")) + ";" + this.evalGroovyProperty(dynamicrenderingPL.getProperty("readonlyif"))).length() > 2) {
                                                                                                                attributes.setProperty("initialbehavior", initialbehavior);
                                                                                                            }
                                                                                                            if ("datelookup".equals(mode)) {
                                                                                                                attributes.setProperty("img", this.element.getProperty("datelookupimg", "WEB-CORE/imageref/flat/32/flat_black_calendar2.svg"));
                                                                                                            } else {
                                                                                                                attributes.setProperty("img", this.element.getProperty("fieldlookupimg", "WEB-CORE/imageref/flat/32/flat_black_external_lookup1.svg"));
                                                                                                            }
                                                                                                            attributes.setProperty("img_cssstyle", this.element.getProperty("img_cssstyle", "lookup_img"));
                                                                                                            attributes.setProperty("span", "1");
                                                                                                            attributes.setProperty("size", "");
                                                                                                            attributes.setProperty("validation", this.column.getProperty("validation"));
                                                                                                            attributes.setProperty("customjs", this.column.getProperty("customjs"));
                                                                                                            attributes.setProperty("extraattributes", this.column.getProperty("extraattributes"));
                                                                                                            attributes.setProperty("class", this.column.getProperty("class"));
                                                                                                            attributes.setProperty("translatevalue", this.column.getProperty("translatevalue"));
                                                                                                            attributes.setProperty("datasetname", this.datasetname);
                                                                                                            PropertyList lookuplink = this.column.getPropertyList("lookuplink");
                                                                                                            PropertyList dropdowndefinition = this.column.getPropertyList("dropdowndefinition");
                                                                                                            if (lookuplink != null) {
                                                                                                                attributes.setProperty("lookuplink", lookuplink);
                                                                                                            } else {
                                                                                                                attributes.setProperty("lookuppageid", this.column.getProperty("lookuppageid"));
                                                                                                            }
                                                                                                            if (dropdowndefinition != null && (dropdowndefinition.getProperty("sdcid").trim().length() > 0 || dropdowndefinition.getProperty("querywhere").trim().length() > 0)) {
                                                                                                                String querywhere = dropdowndefinition.getProperty("querywhere").trim();
                                                                                                                if (querywhere.length() > 0) {
                                                                                                                    dropdowndefinition.setProperty("querywhere", this.evalGroovyProperty(querywhere));
                                                                                                                }
                                                                                                                attributes.setProperty("dropdowndefinition", dropdowndefinition);
                                                                                                            }
                                                                                                            if ((dynamicdropdown = this.column.getPropertyList("dynamicdropdown")) != null && dynamicdropdown.getProperty("elementid").length() > 0 && dynamicdropdown.getProperty("valuefield").length() > 0) {
                                                                                                                attributes.setProperty("dynamicdropdown", dynamicdropdown);
                                                                                                            }
                                                                                                            attributes.setProperty("default", this.column.getProperty("default"));
                                                                                                            isLocked = this.isLocked || this.sdiInfo != null && this.sdiInfo.getValue(this.datasetname, "__lockedby") != null && this.sdiInfo.getValue(this.datasetname, "__lockedby").length() > 0;
                                                                                                            String displayValue = this.column.getProperty("displayvalue");
                                                                                                            isPageViewOnly = this.element.getProperty("viewonly").equals("Y");
                                                                                                            if (displayValue.length() == 0 && (isPageViewOnly || isLocked) && this.column.getProperty("reftypeid").length() > 0) {
                                                                                                                SDITagUtil sdiTagUtil = SDITagUtil.getInstance(this.pageContext);
                                                                                                                DataSet ds = sdiTagUtil.getRefTypeData(this.column, this.sdiInfo);
                                                                                                                StringBuffer refDisplayValue = new StringBuffer();
                                                                                                                if (ds != null) {
                                                                                                                    for (int i = 0; i < ds.getRowCount(); ++i) {
                                                                                                                        String refId = ds.getString(i, "refvalueid");
                                                                                                                        refId = StringUtil.replaceAll(refId, ";", "#semicolon#");
                                                                                                                        String disValue = ds.getString(i, "refdisplayvalue");
                                                                                                                        disValue = disValue != null && disValue.length() > 0 ? disValue : ds.getString(i, "refvalueid");
                                                                                                                        disValue = StringUtil.replaceAll(disValue, ";", "#semicolon#");
                                                                                                                        refDisplayValue.append(refId).append("=").append(!"N".equals(this.column.getProperty("translatevalue")) ? tp.translate(disValue) : disValue).append(";");
                                                                                                                    }
                                                                                                                }
                                                                                                                if (refDisplayValue.length() > 0) {
                                                                                                                    displayValue = refDisplayValue.substring(0, refDisplayValue.length() - 1);
                                                                                                                    this.column.setProperty("reftypeid", "");
                                                                                                                    this.column.setProperty("displayvalue", displayValue);
                                                                                                                }
                                                                                                            }
                                                                                                            attributes.setProperty("displayvalue", displayValue);
                                                                                                            if (this.columnDependsMap != null && this.columnDependsMap.get(columnid) != null) {
                                                                                                                String dependentcolumnlist = this.columnDependsMap.get(columnid);
                                                                                                                attributes.setProperty("dependentcolumnlist", dependentcolumnlist);
                                                                                                                String[] depcolumns = StringUtil.split(dependentcolumnlist, ";");
                                                                                                                String sqlcode = "";
                                                                                                                String dynamiccode = "";
                                                                                                                for (int i = 0; i < depcolumns.length; ++i) {
                                                                                                                    sqlcode = sqlcode + ";" + (this.columnDependsMap.get(depcolumns[i] + "_sqlcode") == null ? "" : this.columnDependsMap.get(depcolumns[i] + "_sqlcode"));
                                                                                                                    dynamiccode = dynamiccode + ";" + (this.columnDependsMap.get(depcolumns[i] + "_dynamiccode") == null ? "" : this.columnDependsMap.get(depcolumns[i] + "_dynamiccode"));
                                                                                                                }
                                                                                                                attributes.setProperty("dependentsqlcode", sqlcode.substring(1));
                                                                                                                attributes.setProperty("dependentdynamiccode", dynamiccode.substring(1));
                                                                                                            }
                                                                                                            attributes.setProperty("sql", this.column.getProperty("sql"));
                                                                                                            attributes.setProperty("refvalues", this.column.getProperty("refvalues"));
                                                                                                            attributes.setProperty("dropdownvalues", this.column.getProperty("dropdownvalues"));
                                                                                                            PropertyListCollection events = this.column.getCollection("events");
                                                                                                            if (events != null) {
                                                                                                                for (int i = 0; i < events.size(); ++i) {
                                                                                                                    PropertyList event = events.getPropertyList(i);
                                                                                                                    attributes.setProperty(event.getProperty("event"), ElementUtil.evaluateExpression(this.datasetname, -1, columnid, event.getProperty("js"), this.sdiInfo));
                                                                                                                }
                                                                                                            }
                                                                                                            if (mode.equals("readonly")) {
                                                                                                                attributes.setProperty("readonly", "true");
                                                                                                            } else {
                                                                                                                attributes.setProperty("readonly", "false");
                                                                                                            }
                                                                                                            if (this.element.getProperty("mode").equals("readonly")) {
                                                                                                                attributes.setProperty("nullvalue", this.column.getProperty("default"));
                                                                                                            }
                                                                                                            detail = false;
                                                                                                            columnDef = null;
                                                                                                            if (this.sdc == null) break block140;
                                                                                                            if (this.datasetname == null || this.datasetname.length() <= 0 || this.datasetname.equals("primary")) break block141;
                                                                                                            PropertyList table = this.sdc.getCollection("tables").getPropertyList(this.datasetname);
                                                                                                            if (table != null) {
                                                                                                                sdccolumns = table.getCollection("columns");
                                                                                                                if (sdccolumns != null) {
                                                                                                                    columnDef = sdccolumns.getPropertyList(columnid);
                                                                                                                    detail = true;
                                                                                                                }
                                                                                                                break block142;
                                                                                                            } else {
                                                                                                                sdccolumns = this.sdc.getCollection("columns");
                                                                                                                if (sdccolumns != null) {
                                                                                                                    columnDef = sdccolumns.getPropertyList(columnid);
                                                                                                                }
                                                                                                            }
                                                                                                            break block142;
                                                                                                        }
                                                                                                        PropertyListCollection sdccolumns = this.sdc.getCollection("columns");
                                                                                                        if (sdccolumns != null) {
                                                                                                            columnDef = sdccolumns.getPropertyList(columnid);
                                                                                                        }
                                                                                                        PropertyListCollection links = this.sdc.getCollection("links");
                                                                                                        if (this.sdiInfo == null || links == null || links.size() <= 0) break block142;
                                                                                                        PropertyList link3 = links.find("sdccolumnid", columnid);
                                                                                                        if (link3 != null && link3.getProperty("linktype", "").equalsIgnoreCase("F")) {
                                                                                                            attributes.setProperty("fkmaster", columnid);
                                                                                                            String fkkey = this.sdiInfo.getValue(this.datasetname, link3.getProperty("sdccolumnid"));
                                                                                                            if (link3.getProperty("sdccolumnid2").length() > 0) {
                                                                                                                attributes.setProperty("fkmaster2", link3.getProperty("sdccolumnid2"));
                                                                                                                fkkey = fkkey + ";" + this.sdiInfo.getValue(this.datasetname, link3.getProperty("sdccolumnid2"));
                                                                                                            }
                                                                                                            attributes.setProperty("fkkey", fkkey);
                                                                                                            break block142;
                                                                                                        } else {
                                                                                                            link3 = links.find("sdccolumnid2", columnid);
                                                                                                            if (link3 != null && link3.getProperty("linktype", "").equalsIgnoreCase("F")) {
                                                                                                                attributes.setProperty("fkmaster2", columnid);
                                                                                                                if (link3.getProperty("sdccolumnid").length() > 0) {
                                                                                                                    attributes.setProperty("fkmaster", link3.getProperty("sdccolumnid"));
                                                                                                                }
                                                                                                                attributes.setProperty("fkkey", this.sdiInfo.getValue(this.datasetname, link3.getProperty("sdccolumnid")) + ";" + this.sdiInfo.getValue(this.datasetname, link3.getProperty("sdccolumnid2")));
                                                                                                            }
                                                                                                        }
                                                                                                        break block142;
                                                                                                    }
                                                                                                    columnDef = this.columnDefinition;
                                                                                                    if (columnDef == null && attributes.getProperty("title").length() == 0) {
                                                                                                        attributes.setProperty("title", columnid);
                                                                                                    }
                                                                                                }
                                                                                                if (mode.equalsIgnoreCase("lookup")) {
                                                                                                    PropertyListCollection lookupcols;
                                                                                                    PropertyList lookupprops = this.column.getPropertyList("lookuplink");
                                                                                                    PropertyListCollection propertyListCollection = lookupcols = lookupprops != null ? lookupprops.getCollection("columns") : null;
                                                                                                    if (lookupcols != null) {
                                                                                                        StringBuffer relcols = new StringBuffer();
                                                                                                        StringBuffer relvals = new StringBuffer();
                                                                                                        for (int lc = 0; lc < lookupcols.size(); ++lc) {
                                                                                                            PropertyList lookupcol = lookupcols.getPropertyList(lc);
                                                                                                            String mapfieldid = lookupcol.getProperty("mapfieldid");
                                                                                                            if (mapfieldid.length() <= 0) continue;
                                                                                                            if (relcols.length() > 0) {
                                                                                                                relcols.append(";");
                                                                                                                relvals.append("#semicolon#");
                                                                                                            }
                                                                                                            relcols.append(mapfieldid);
                                                                                                            String lv = this.sdiInfo.getValue(this.datasetname, mapfieldid);
                                                                                                            relvals.append(lv);
                                                                                                        }
                                                                                                        attributes.setProperty("relatedcolumns", relcols.toString());
                                                                                                        attributes.setProperty("relatedvalues", relvals.toString());
                                                                                                    }
                                                                                                }
                                                                                                fkcolumn = false;
                                                                                                if (columnDef != null || mode.length() <= 0 || mode.equalsIgnoreCase("readonly") || mode.equalsIgnoreCase("retrievedata") || mode.equalsIgnoreCase("pseudocolumn")) break block143;
                                                                                                columnt = this.column.getProperty("columnid").trim();
                                                                                                String string = colalias = sdialias ? columnt : RequestParser.parseAlias(columnt);
                                                                                                if (this.sdc == null || columnt.indexOf(".") <= -1 || columnt.startsWith("(") && colalias.indexOf(".") <= -1) break block136;
                                                                                                if (columnt.startsWith("(")) {
                                                                                                    columnt = colalias;
                                                                                                } else if (!columnt.equalsIgnoreCase(columnid)) {
                                                                                                    columnt = RequestParser.parseColumn(columnt);
                                                                                                }
                                                                                                parts = StringUtil.split(columnt, ".");
                                                                                                if (parts.length != 2) break block136;
                                                                                                if (!parts[0].equalsIgnoreCase("trackitem")) break block144;
                                                                                                if (!parts[1].equalsIgnoreCase("trackitemid")) break block145;
                                                                                                attributes.setProperty("tip", tp.translate("Trackitem Id autogenerated."));
                                                                                                attributes.setProperty("readonly", "true");
                                                                                                break block136;
                                                                                            }
                                                                                            if (this.sdiInfo.getBigDecimal("primary", "__ticount") != null && this.sdiInfo.getBigDecimal("primary", "__ticount").intValue() <= 1 || this.rowStatus.equalsIgnoreCase("I")) {
                                                                                                PropertyListCollection ticols = this.getSDCProcessor().getColumns("TrackItemSDC");
                                                                                                if (ticols != null) {
                                                                                                    String linkkey2;
                                                                                                    columnDef = ticols.find("columnid", parts[1]);
                                                                                                    fkcolumn = true;
                                                                                                    String string = linkkey2 = this.sdiInfo.getValue(this.datasetname, this.sdc.getProperty("keycolid1")).length() > 0 ? this.sdiInfo.getValue(this.datasetname, this.sdc.getProperty("keycolid1")) : "(auto)" + this.sdiInfo.getCurrentRow();
                                                                                                    if (linkkey2.length() > 0 && this.sdc.getProperty("keycolid2").length() > 0) {
                                                                                                        linkkey2 = linkkey2 + ";" + (this.sdiInfo.getValue(this.datasetname, this.sdc.getProperty("keycolid1")).length() > 0 ? this.sdiInfo.getValue(this.datasetname, this.sdc.getProperty("keycolid1")) : "(auto)");
                                                                                                    }
                                                                                                    attributes.setProperty("fklink", "" + parts[0] + "." + parts[1]);
                                                                                                    attributes.setProperty("fkkey", linkkey2);
                                                                                                }
                                                                                                break block136;
                                                                                            } else {
                                                                                                attributes.setProperty("tip", tp.translate("SDI has multiple TrackItems and therefore TrackItem columns cannot be edited."));
                                                                                                attributes.setProperty("readonly", "true");
                                                                                            }
                                                                                            break block136;
                                                                                        }
                                                                                        if (!parts[0].equalsIgnoreCase("sdialias")) break block146;
                                                                                        columnDef = new PropertyList();
                                                                                        columnDef.setProperty("columnlength", "80");
                                                                                        columnDef.setProperty("columndesc", "Alias Id");
                                                                                        columnDef.setProperty("pkflag", "N");
                                                                                        columnDef.setProperty("datatype", "C");
                                                                                        columnDef.setProperty("columnid", "aliasid");
                                                                                        fkcolumn = true;
                                                                                        String string = linkkey = this.sdiInfo.getValue(this.datasetname, this.sdc.getProperty("keycolid1")).length() > 0 ? this.sdiInfo.getValue(this.datasetname, this.sdc.getProperty("keycolid1")) : "(auto)" + this.sdiInfo.getCurrentRow();
                                                                                        if (linkkey.length() > 0 && this.sdc.getProperty("keycolid2").length() > 0) {
                                                                                            linkkey = linkkey + ";" + (this.sdiInfo.getValue(this.datasetname, this.sdc.getProperty("keycolid1")).length() > 0 ? this.sdiInfo.getValue(this.datasetname, this.sdc.getProperty("keycolid1")) : "(auto)");
                                                                                        }
                                                                                        attributes.setProperty("fklink", "" + parts[0] + "." + parts[1]);
                                                                                        attributes.setProperty("fkkey", linkkey);
                                                                                        break block136;
                                                                                    }
                                                                                    if (this.sdiInfo == null || this.sdiInfo.getSDIData() == null || this.sdiInfo.getSDIData().getPrimaryFKRsetid(parts[0]).length() <= 0) break block147;
                                                                                    PropertyListCollection links = this.sdc.getCollection("links");
                                                                                    if (links == null || links.size() <= 0 || (link = links.find("sdccolumnid", parts[0])) == null || !link.getProperty("linktype", "").equalsIgnoreCase("F")) break block136;
                                                                                    String linksdcid = link.getProperty("linksdcid");
                                                                                    PropertyListCollection linkcols = null;
                                                                                    if (this.linkcolumns == null) {
                                                                                        this.linkcolumns = new HashMap();
                                                                                        linkcols = this.getSDCProcessor().getColumns(linksdcid);
                                                                                        this.linkcolumns.put(linksdcid, linkcols);
                                                                                    } else if (this.linkcolumns.containsKey(linksdcid)) {
                                                                                        linkcols = this.linkcolumns.get(linksdcid);
                                                                                    } else {
                                                                                        linkcols = this.getSDCProcessor().getColumns(linksdcid);
                                                                                        this.linkcolumns.put(linksdcid, linkcols);
                                                                                    }
                                                                                    if (linkcols == null) break block136;
                                                                                    boolean fkedit = false;
                                                                                    boolean checked = true;
                                                                                    if (this.getSDCProcessor().getProperty(linksdcid, "versionedflag", "N").equalsIgnoreCase("Y") && !this.sdiInfo.getValue(this.datasetname, link.getProperty("tableid") + ".versionstatus").equalsIgnoreCase("P")) {
                                                                                        checked = false;
                                                                                        this.logger.info("Column not editable because versionstatus of FK link is not provisional.");
                                                                                    }
                                                                                    if (checked) {
                                                                                        String linkkey;
                                                                                        if (this.sdiInfo.getValue(this.datasetname, parts[0]).length() > 0) {
                                                                                            if (link.getProperty("sdccolumnid2").length() > 0) {
                                                                                                if (this.sdiInfo.getValue(this.datasetname, link.getProperty("sdccolumnid2")).length() > 0) {
                                                                                                    fkedit = true;
                                                                                                }
                                                                                            } else {
                                                                                                fkedit = true;
                                                                                            }
                                                                                        }
                                                                                        if ((linkkey = this.sdiInfo.getValue(this.datasetname, parts[0])).length() > 0 && link.getProperty("sdccolumnid2").length() > 0) {
                                                                                            linkkey = linkkey + ";" + this.sdiInfo.getValue(this.datasetname, link.getProperty("sdccolumnid2"));
                                                                                        }
                                                                                        if (fkedit) {
                                                                                            fkcolumn = true;
                                                                                            columnDef = linkcols.getPropertyList(parts[1]);
                                                                                            attributes.setProperty("fklink", "" + parts[0] + "." + parts[1]);
                                                                                            attributes.setProperty("fkkey", linkkey);
                                                                                            break block136;
                                                                                        } else {
                                                                                            if (linkcols.getPropertyList(parts[1]) == null) {
                                                                                                attributes.setProperty("fkkey", linkkey);
                                                                                            } else {
                                                                                                attributes.setProperty("readonly", "true");
                                                                                                attributes.setProperty("fkkey", "");
                                                                                            }
                                                                                            attributes.setProperty("fklink", "" + parts[0] + "." + parts[1]);
                                                                                        }
                                                                                    }
                                                                                    break block136;
                                                                                }
                                                                                if (!parts[1].startsWith("_") || attributes.getProperty("relatedcolumns").length() <= 0) {
                                                                                    attributes.setProperty("readonly", "true");
                                                                                }
                                                                                attributes.setProperty("fklink", "" + parts[0] + "." + parts[1]);
                                                                                attributes.setProperty("fkkey", "");
                                                                                break block136;
                                                                            }
                                                                            columnt = this.column.getProperty("columnid").trim();
                                                                            if (this.sdc != null && !columnt.startsWith("(") && columnt.indexOf(".") > -1) {
                                                                                attributes.setProperty("fkkey", "");
                                                                            }
                                                                        }
                                                                        if (!mode.equals("hidden")) break block148;
                                                                        attributes.setProperty("mode", "hidden");
                                                                        break block149;
                                                                    }
                                                                    if (columnDef == null || forcePseudo) break block150;
                                                                    attributes.setProperty("mode", this.column.getProperty("mode", "input"));
                                                                    if (this.sdiInfo != null && this.sdiInfo.getQueryData(this.datasetname).getQuerydata().getColumnLength(columnid) / bytesperchar > 80) {
                                                                        attributes.setProperty("mode", this.column.getProperty("mode", "inputarea"));
                                                                    }
                                                                    int colLength = 20;
                                                                    if (detail) {
                                                                        datatype = columnDef.getProperty("linkdatatype", "");
                                                                        length = columnDef.getProperty("linkcolumnlength", "" + colLength);
                                                                        if (columnDef.containsKey("linklink") && columnDef.get("linklink") != null && columnDef.get("linklink") instanceof String) {
                                                                            int link = -1;
                                                                            try {
                                                                                link = Integer.parseInt(columnDef.getProperty("linklink", "-1"));
                                                                            }
                                                                            catch (Exception e) {
                                                                                link = -1;
                                                                            }
                                                                            columnLink = link > -1 && this.sdc.getCollection("detaillinks") != null && this.sdc.getCollection("detaillinks").size() > link ? this.sdc.getCollection("detaillinks").getPropertyList(link) : null;
                                                                        } else {
                                                                            columnLink = null;
                                                                        }
                                                                        pkFlag = columnDef.getProperty("linkpkflag", "N");
                                                                    } else {
                                                                        datatype = columnDef.getProperty("datatype", "");
                                                                        length = columnDef.getProperty("columnlength", "" + colLength);
                                                                        columnLink = columnDef.getPropertyList("link");
                                                                        pkFlag = columnDef.getProperty("pkflag", "N");
                                                                    }
                                                                    if (this.column.getProperty("dataentry", "false").equals("false")) {
                                                                        try {
                                                                            colLength = Integer.parseInt(length);
                                                                        }
                                                                        catch (NumberFormatException nfe) {
                                                                            this.logger.debug("Could not find column length.");
                                                                        }
                                                                        this.setLayoutAttributes(datatype, colLength, this.column.getProperty("colspan"), attributes);
                                                                    }
                                                                    if (attributes.getProperty("title").length() == 0) {
                                                                        if (detail) {
                                                                            attributes.setProperty("title", columnid);
                                                                        } else {
                                                                            attributes.setProperty("title", columnDef.getProperty("columndesc", columnid));
                                                                        }
                                                                    }
                                                                    if (!datatype.equals("C")) break block151;
                                                                    if (this.column.getProperty("dataentry", "false").equals("true")) {
                                                                        try {
                                                                            attributes.setProperty("maxlen", Integer.parseInt(length) + "");
                                                                        }
                                                                        catch (NumberFormatException nfe) {
                                                                            attributes.setProperty("maxlen", colLength + "");
                                                                        }
                                                                    } else {
                                                                        attributes.setProperty("maxlen", colLength + "");
                                                                    }
                                                                    if (columnLink == null) break block152;
                                                                    String linksdcid = columnLink.getProperty("linksdcid", columnLink.getProperty("linksdcid", ""));
                                                                    String linktype = columnLink.getProperty("type", columnLink.getProperty("linktype", ""));
                                                                    if (linksdcid.length() > 0) {
                                                                        if (linksdcid.equalsIgnoreCase("reftype") && !linktype.equalsIgnoreCase("F")) {
                                                                            if (mode.equals("checkbox") || mode.equals("dropdownlist") || mode.equals("dropdownlistvalidated") || mode.indexOf("radiobutton") >= 0 || mode.equals("maskvalue") || mode.equals("displayicon") || mode.equals("icon")) {
                                                                                attributes.setProperty("mode", mode);
                                                                            } else if (!attributes.getProperty("readonly").equals("true")) {
                                                                                if (linktype.equals("V")) {
                                                                                    attributes.setProperty("readonly", "true");
                                                                                }
                                                                                attributes.setProperty("mode", "dropdowncombo");
                                                                            } else {
                                                                                attributes.setProperty("mode", "text");
                                                                            }
                                                                            String reftype = columnLink.getProperty("reftypeid", columnLink.getProperty("reftypeid", ""));
                                                                            if (this.column.getProperty("reftypeid").length() == 0) {
                                                                                this.column.setProperty("reftypeid", reftype);
                                                                            }
                                                                            attributes.setProperty("reftypeid", this.column.getProperty("reftypeid"));
                                                                            attributes.setProperty("imgtext", tp.translate("Looking up a reference value"));
                                                                            break block137;
                                                                        } else {
                                                                            if (mode.equals("dropdownlist") || mode.equals("dropdownlistvalidated") || mode.equals("dropdowncombo") || mode.indexOf("radiobutton") >= 0 || mode.equals("maskvalue") || mode.equals("displayicon") || mode.equals("icon")) {
                                                                                attributes.setProperty("mode", mode);
                                                                                if (mode.equals("dropdowncombo")) {
                                                                                    attributes.setProperty("readonly", "true");
                                                                                }
                                                                            } else {
                                                                                attributes.setProperty("mode", "lookup");
                                                                            }
                                                                            if ((attributes.getProperty("mode").equals("dropdownlist") || attributes.getProperty("mode").equals("dropdownlistvalidated")) && attributes.getProperty("sql").trim().length() > 0) {
                                                                                attributes.setProperty("sdcid", "");
                                                                            } else {
                                                                                attributes.setProperty("sdcid", linksdcid);
                                                                            }
                                                                            attributes.setProperty("imgtext", tp.translate("Looking up a") + " " + tp.translate(linksdcid));
                                                                            attributes.setProperty("sdccolumnid2", columnLink.getProperty("sdccolumnid2", columnLink.getProperty("sdccolumnid2", "")));
                                                                            attributes.setProperty("versionedflag", columnLink.getProperty("versionedflag", "N"));
                                                                        }
                                                                    }
                                                                    break block137;
                                                                }
                                                                if (!attributes.getProperty("mode").equals("input") && !attributes.getProperty("mode").equals("textarea")) break block153;
                                                                if (this.sdiInfo != null && this.sdiInfo.getCurrentRow(this.datasetname) <= 0 && this.column.getProperty("validation").indexOf("Invalid") < 0) {
                                                                    if (pkFlag.equals("Y") && !"refvalueid".equals(columnid)) {
                                                                        this.column.setProperty("validation", this.column.getProperty("validation").length() > 0 ? "ValidKey;" + this.column.getProperty("validation") : "ValidKey");
                                                                        break block137;
                                                                    } else {
                                                                        this.column.setProperty("validation", this.column.getProperty("validation").length() > 0 ? "InvalidChars;" + this.column.getProperty("validation") : "InvalidChars");
                                                                    }
                                                                }
                                                                break block137;
                                                            }
                                                            if (attributes.getProperty("mode").equals("dropdownlist") || attributes.getProperty("mode").equals("dropdownlistvalidated") || attributes.getProperty("mode").equals("dropdowncombo")) {
                                                                attributes.setProperty("reftypeid", this.column.getProperty("reftypeid"));
                                                                break block137;
                                                            } else if (attributes.getProperty("mode").equals("checkbox") && attributes.getProperty("reftypeid", "").length() == 0) {
                                                                attributes.setProperty("reftypeid", this.column.getProperty("reftypeid"));
                                                            }
                                                            break block137;
                                                        }
                                                        if (!datatype.equals("D")) break block154;
                                                        attributes.setProperty("mode", this.column.getProperty("mode").equals("maskvalue") ? "maskvalue" : "datelookup");
                                                        attributes.setProperty("imgtext", tp.translate("Lookup a date"));
                                                        attributes.setProperty("img", this.element.getProperty("datelookupimg", "WEB-CORE/imageref/flat/32/flat_black_calendar2.svg"));
                                                        attributes.setProperty("img_cssstyle", this.element.getProperty("img_cssstyle", "datelookup_img"));
                                                        attributes.setProperty("format", this.column.getProperty("format"));
                                                        if (this.column.getProperty("validation").length() == 0) {
                                                            this.column.setProperty("validation", "Date");
                                                            attributes.setProperty("validation", "Date");
                                                            break block137;
                                                        } else if (!this.column.getProperty("validation").contains("Date") && !this.column.getProperty("validation").contains("Do Not Validate")) {
                                                            String validation = this.column.getProperty("validation");
                                                            validation = validation + ";Date";
                                                            this.column.setProperty("validation", validation);
                                                            attributes.setProperty("validation", validation);
                                                        }
                                                        break block137;
                                                    }
                                                    if (datatype.equals("N") || datatype.equals("R")) {
                                                        if (this.column.getProperty("validation").length() == 0) {
                                                            this.column.setProperty("validation", "Number( to )");
                                                            attributes.setProperty("validation", "Number( to )");
                                                        } else if (!this.column.getProperty("validation").contains("Number") && !this.column.getProperty("validation").contains("Do Not Validate")) {
                                                            String validation = this.column.getProperty("validation");
                                                            validation = validation + ";Number( to )";
                                                            this.column.setProperty("validation", validation);
                                                            attributes.setProperty("validation", validation);
                                                        }
                                                    }
                                                }
                                                if (!pkFlag.equals("Y") || "prompt".equals(this.element.getProperty("mode"))) break block155;
                                                if (!this.rowStatus.equals("I")) break block156;
                                                if (detail) {
                                                    if (this.column.getProperty("mode").length() == 0) {
                                                        attributes.setProperty("readonly", "true");
                                                    }
                                                    break block155;
                                                } else {
                                                    String keygenrule = this.sdc.getProperty("keygenerationrule").trim();
                                                    if (keygenrule.length() > 0 && keygenrule.charAt(0) == 'A' && columnid != null && columnid.equals(this.sdc.getProperty("keycolid1")) && !this.element.getProperty("templateflag").equals("Y")) {
                                                        attributes.setProperty("mode", "text");
                                                        attributes.setProperty("nullvalue", "(Auto)");
                                                    }
                                                }
                                                break block155;
                                            }
                                            if (detail) {
                                                if (this.column.getProperty("mode", "").length() == 0) {
                                                    attributes.setProperty("readonly", "true");
                                                }
                                                break block155;
                                            } else {
                                                attributes.setProperty("mode", "text");
                                            }
                                            break block155;
                                        }
                                        attributes.setProperty("mode", this.column.getProperty("mode", "text"));
                                        attributes.setProperty("reftypeid", this.column.getProperty("reftypeid"));
                                        if (this.column.getProperty("pseudocolumn").length() == 0 && !this.column.getProperty("pseudocolumn").equals("true")) {
                                            if (this.sdiInfo != null) {
                                                int colLength = this.sdiInfo.getQueryData(this.datasetname).getQuerydata().getColumnLength(columnid) / bytesperchar;
                                                if (this.sdiInfo.getQueryData(this.datasetname).getQuerydata().getColumnType(columnid) != -1) {
                                                    if (colLength > 200 && attributes.getProperty("mode").equals("text")) {
                                                        attributes.setProperty("readonly", "true");
                                                    }
                                                    this.setLayoutAttributes("C", colLength, this.column.getProperty("colspan"), attributes);
                                                }
                                            }
                                            if (!(columnid.indexOf(".") <= 0 || attributes.getProperty("fkkey").length() != 0 || "modern_search_queryarginput".equals(this.column.getProperty("class")) || StringUtil.split(columnid, ".")[1].startsWith("_") && attributes.getProperty("relatedcolumns").length() > 0)) {
                                                attributes.setProperty("readonly", "true");
                                            }
                                            if (attributes.getProperty("mode").equalsIgnoreCase("lookup") && this.column.getPropertyList("lookuplink") != null) {
                                                attributes.setProperty("sdcid", this.column.getPropertyList("lookuplink").getProperty("sdcid", ""));
                                            }
                                        } else {
                                            if (this.column.getProperty("mode").length() == 0 || this.column.getProperty("mode").equals("readonly")) {
                                                attributes.setProperty("mode", "pseudocolumn");
                                                attributes.setProperty("pseudocolumn", this.evalGroovyProperty(this.column.getProperty("pseudocolumn")));
                                            }
                                            this.setLayoutAttributes("C", 20, this.column.getProperty("colspan"), attributes);
                                        }
                                        if (attributes.getProperty("title").length() == 0) {
                                            attributes.setProperty("title", columnid);
                                        }
                                    }
                                    if (attributes.getProperty("readonly").equals("true") && !attributes.getProperty("mode").equals("dropdowncombo") && !attributes.getProperty("mode").equals("inputarea") && !attributes.getProperty("mode").equals("pseudocolumn")) {
                                        attributes.setProperty("mode", "text");
                                        if (this.column.getProperty("reftypeid").length() > 0) {
                                            attributes.setProperty("reftypeid", this.column.getProperty("reftypeid"));
                                        }
                                    }
                                    if (attributes.getProperty("mode").equals("lookup") && !"N".equals(this.column.getProperty("forcereadonly")) || this.column.getProperty("forcereadonly").equalsIgnoreCase("Y")) {
                                        attributes.setProperty("readonly", "true");
                                    }
                                }
                                if (this.rowStatus.equals("I") || !isPageViewOnly && !isLocked || attributes.getProperty("mode").equals("hidden")) break block157;
                                if (attributes.getProperty("mode").equals("inputarea") || attributes.getProperty("mode").equals("pseudocolumn")) break block158;
                                if (!"password".equals(attributes.getProperty("mode")) && !"maskvalue".equals(attributes.getProperty("mode"))) break block159;
                                attributes.setProperty("mode", "maskvalue");
                                break block138;
                            }
                            if (!attributes.getProperty("mode").equalsIgnoreCase("richtexthtml") && !attributes.getProperty("mode").equalsIgnoreCase("formattedtext") && !attributes.getProperty("mode").equalsIgnoreCase("html")) break block160;
                            attributes.setProperty("mode", "html");
                            break block138;
                        }
                        if (attributes.getProperty("mode").equalsIgnoreCase("richtexthtml_dm") || attributes.getProperty("mode").equalsIgnoreCase("html_dm")) {
                            attributes.setProperty("mode", "html_dm");
                            break block138;
                        } else if (attributes.getProperty("mode").equalsIgnoreCase("icon") || attributes.getProperty("mode").equalsIgnoreCase("image")) {
                            attributes.setProperty("disabled", "true");
                            break block138;
                        } else {
                            attributes.setProperty("mode", "text");
                        }
                        break block138;
                    }
                    attributes.setProperty("readonly", "true");
                    attributes.remove("lookuplink");
                    attributes.remove("lookuppageid");
                }
                if (isLocked) {
                    attributes.setProperty("class", "maint_lockedfield");
                }
            }
            String disableonsave = this.column.getProperty("disableonsave", "N");
            mode = attributes.getProperty("mode", "");
            String readonly = attributes.getProperty("readonly", "false");
            if (!(!disableonsave.equalsIgnoreCase("Y") || mode.equalsIgnoreCase("hidden") && mode.equalsIgnoreCase("retrievedata") && mode.equalsIgnoreCase("pseudocolumn") && !mode.equalsIgnoreCase("readonly") || !this.rowStatus.equals("S") && !this.rowStatus.equals("D") || readonly.equalsIgnoreCase("true") && !mode.equalsIgnoreCase("lookup"))) {
                attributes.setProperty("mode", "text");
                attributes.setProperty("readonly", "true");
            }
        }
        if (this.column.getProperty("disable", "").length() > 0 && (disabled = this.evalGroovyProperty(this.column.getProperty("disable", ""))).equalsIgnoreCase("Y")) {
            attributes.setProperty("disabled", "true");
        }
        if (attributes.getProperty("mode").equalsIgnoreCase("formattedtext")) {
            PropertyList oa = new PropertyList();
            oa.setProperty("mode", "formattedtext");
            attributes.setProperty("outputattributes", oa);
        }
        if ((attributes.getProperty("mode").startsWith("richtexthtml") || attributes.getProperty("mode").startsWith("formattedtext") || attributes.getProperty("mode").startsWith("html")) && this.column.getPropertyList("phrases") != null) {
            attributes.setProperty("phrasetype", this.column.getPropertyList("phrases").getProperty("phrasetype", ""));
            attributes.setProperty("phraselookup", this.column.getPropertyList("phrases").getProperty("phraselookup", ""));
        }
        return attributes;
    }

    private void setLayoutAttributes(String datatype, int colLength, String colspan, PropertyList attributes) {
        int formCols = 1;
        int colSpans = 1;
        if (this.element.getProperty("formcols").length() > 0) {
            try {
                formCols = Integer.parseInt(this.element.getProperty("formcols"));
            }
            catch (NumberFormatException nfe) {
                this.logger.debug("Could not obtain form cols.");
            }
        }
        if (colspan.length() > 0) {
            try {
                colSpans = Integer.parseInt(colspan);
            }
            catch (NumberFormatException nfe) {
                this.logger.debug("Could not obtain colspan.");
            }
            if (colSpans > formCols) {
                colSpans = formCols;
            }
        }
        if ("inputarea".equals(attributes.getProperty("mode")) || colLength > 200 || datatype.equals("T") || datatype.equals("B")) {
            String span;
            if (colspan.length() > 0) {
                span = "" + colSpans;
            } else {
                span = this.element.getProperty("formcols");
                colSpans = formCols;
            }
            attributes.setProperty("span", span);
            attributes.setProperty("cols", String.valueOf(colSpans > 1 ? (colSpans - 1) * 43 + 40 : (this.element.getProperty("mode").equalsIgnoreCase("prompt") ? 60 : 83)));
            if (this.browser != null && this.browser.isPhone() && this.element.getProperty("style").indexOf("Form") == 0 && formCols == 1 && "input".equals(attributes.getProperty("mode"))) {
                attributes.setProperty("class", "input_maxwidth");
            }
            attributes.setProperty("mode", attributes.getProperty("mode").equals("readonly") ? "inputarea" : attributes.getProperty("mode", "inputarea"));
            if (attributes.getProperty("mode").equals("input") || attributes.getProperty("mode").equals("text") || attributes.getProperty("mode").equals("lookup")) {
                attributes.setProperty("size", String.valueOf(colSpans > 1 ? (colSpans - 1) * 43 + 40 : 83));
            } else if (this.element.getProperty("mode").equalsIgnoreCase("prompt") || this.element.getProperty("style").equalsIgnoreCase("form") || this.element.getProperty("style").equalsIgnoreCase("formwithtabgroups") || this.element.getProperty("style").equalsIgnoreCase("formwithfieldgroups")) {
                if (datatype.equals("T") || datatype.equals("B")) {
                    attributes.setProperty("cols", "100");
                    attributes.setProperty("rows", "40");
                } else {
                    attributes.setProperty("rows", "5");
                }
                String onkeyupjs = attributes.getProperty("onkeyup");
                attributes.setProperty("onkeyup", (onkeyupjs.length() > 0 ? onkeyupjs + ";" : "") + "currentTextAreaChanged( this )");
            } else {
                attributes.setProperty("rows", "1");
                if (this.column.getProperty("size").length() > 0) {
                    try {
                        Integer.parseInt(this.column.getProperty("size"));
                        attributes.setProperty("cols", this.browser != null && this.browser.isPhone() ? "29" : this.column.getProperty("size"));
                    }
                    catch (NumberFormatException e) {
                        this.logger.debug("Could not find column size.");
                    }
                }
                if (attributes.getProperty("mode").equals("inputarea")) {
                    attributes.setProperty("onclick", "showGridTextArea( this, " + colLength + " )");
                } else if (attributes.getProperty("mode").equalsIgnoreCase("formattedtext") || attributes.getProperty("mode").equalsIgnoreCase("html")) {
                    attributes.setProperty("size", "800;30");
                }
            }
            if (this.browser != null && this.browser.isPhone() && attributes.getProperty("mode").equals("inputarea")) {
                attributes.setProperty("cols", "");
                attributes.setProperty("style", "display: block;min-width:100%;-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;");
            }
        } else if (colLength > 40) {
            String span = colspan.length() == 0 ? (formCols > 1 ? "2" : "1") : "" + colSpans;
            attributes.setProperty("span", span);
            if (!(attributes.getProperty("mode").equals("dropdownlist") || attributes.getProperty("mode").equals("dropdownlistvalidated") || attributes.getProperty("mode").equals("checkbox"))) {
                if (span.equals("1")) {
                    if ("text".equals(attributes.getProperty("mode")) || "readonly".equals(attributes.getProperty("mode"))) {
                        attributes.setProperty("size", "");
                        attributes.setProperty("style", "min-width:100%;-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;");
                    } else {
                        if (this.browser != null && this.browser.isPhone() && this.element.getProperty("style").indexOf("Form") == 0 && formCols == 1 && "input".equals(attributes.getProperty("mode"))) {
                            attributes.setProperty("class", "input_maxwidth");
                        }
                        attributes.setProperty("size", "23");
                    }
                } else {
                    attributes.setProperty("size", String.valueOf(colSpans > 1 ? (colSpans - 1) * 43 + 40 : 83));
                }
            }
        } else {
            String span = colspan.length() == 0 ? "1" : "" + colSpans;
            attributes.setProperty("span", span);
            if (!(attributes.getProperty("mode").equals("dropdownlist") || attributes.getProperty("mode").equals("dropdownlistvalidated") || attributes.getProperty("mode").equals("checkbox"))) {
                if (colLength > 0) {
                    attributes.setProperty("size", colLength + (colLength <= 20 ? 3 : (colLength <= 25 ? 4 : (colLength <= 30 ? 5 : (colLength <= 35 ? 6 : 7)))) + "");
                } else {
                    attributes.setProperty("size", "23");
                }
            }
        }
        if ((attributes.getProperty("mode").equalsIgnoreCase("formattedtext") || attributes.getProperty("mode").equalsIgnoreCase("html") || attributes.getProperty("mode").equalsIgnoreCase("richtexthtml") || attributes.getProperty("mode").equalsIgnoreCase("richtexthtml_dm")) && this.column.getProperty("size", "").length() > 0) {
            attributes.setProperty("size", this.column.getProperty("size"));
        }
        if (!(this.column.getProperty("mode").equals("dropdownlist") || this.column.getProperty("mode").equals("dropdownlistvalidated") || this.element.containsKey("style") && this.element.getProperty("style").toLowerCase().indexOf("grid") < 0 || this.column.getProperty("size").length() <= 0)) {
            try {
                Integer.parseInt(this.column.getProperty("size"));
                attributes.setProperty("size", this.column.getProperty("size"));
            }
            catch (NumberFormatException e) {
                this.logger.debug("Could not obtain size.");
            }
        }
        if (this.browser != null && this.browser.isPhone()) {
            attributes.setProperty("size", "");
        }
    }

    private String evalGroovyProperty(String propertyvalue) {
        if (propertyvalue.length() > 0 && propertyvalue.indexOf("$G{") == 0) {
            try {
                propertyvalue = this.datasetname.equalsIgnoreCase("primary") ? GroovyUtil.evaluate(propertyvalue, this.connectionInfo, this.sdiInfo, this.element, this.requestContext.getPropertyList().getPropertyList("pagedata"), this.sdc) : GroovyUtil.evaluate(propertyvalue, this.connectionInfo, this.sdiInfo, this.element, this.requestContext.getPropertyList().getPropertyList("pagedata"), this.sdc, this.datasetname);
            }
            catch (SapphireException se) {
                propertyvalue = "Error evaluating groovy:" + propertyvalue + ". " + se.getMessage();
                this.logger.error("Error evaluating groovy:" + propertyvalue);
            }
        }
        return "true".equals(propertyvalue) ? "Y" : ("false".equals(propertyvalue) ? "N" : propertyvalue);
    }
}

