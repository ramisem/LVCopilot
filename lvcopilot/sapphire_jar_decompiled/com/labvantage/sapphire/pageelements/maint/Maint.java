/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.admin.system.SQLRegister;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.maint.LockedImage;
import com.labvantage.sapphire.pageelements.maint.MaintAjaxRequest;
import com.labvantage.sapphire.pageelements.maint.MaintColumn;
import com.labvantage.sapphire.pageelements.maint.RegexConverter;
import com.labvantage.sapphire.pageelements.maint.SmartScrollGridRenderer;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.tagext.SDITagUtil;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.JstlUtil;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Maint
extends BaseElement {
    private boolean insidesdiform = true;
    private String rowStatus = "";
    private MaintStyle style = MaintStyle.FORM;
    private String prefix = "";
    private String elementid = "";
    private String sdcid = "";
    private String datasetName = "primary";
    private boolean showNoteImage = false;
    private DataSet notes;
    private HashMap findNotesMap;
    private HashMap<String, String> columnDependsMap = new HashMap();
    private HashMap<String, String> fieldInitialbehaviorMap = new HashMap();
    private HashMap<String, String> fieldDefaultValueMap = new HashMap();

    public Maint() {
    }

    public static StringBuffer getCustomColumnList(PropertyList maint, PropertyList sdc) {
        StringBuffer columnslist = new StringBuffer();
        ArrayList<String> unique = new ArrayList<String>();
        for (int i = 0; i < maint.getCollection("columns").size(); ++i) {
            PropertyList column = maint.getCollection("columns").getPropertyList(i);
            String columnid = column.getProperty("columnid").trim();
            if (!columnid.startsWith("(") && columnid.indexOf(".") == -1) {
                PropertyList col;
                if (!columnid.startsWith("_") || unique.contains(columnid) || (col = sdc.getCollection("columns").find("columnid", columnid)) != null) continue;
                unique.add(columnid);
                continue;
            }
            if (!columnid.startsWith("(")) continue;
            String alias = RequestParser.parseAlias(columnid);
            int j = alias.indexOf(".");
            if (!alias.startsWith("_") && (j <= -1 || !alias.substring(j + 1).startsWith("_"))) continue;
            unique.add(columnid);
        }
        for (String u : unique) {
            if (columnslist.length() > 0) {
                columnslist.append(";");
            }
            columnslist.append(u);
        }
        return columnslist;
    }

    public static String getBlockColumns(PropertyList maint, boolean columnBlockingEnabled) {
        StringBuilder blockColumns = new StringBuilder();
        for (int i = 0; i < maint.getCollectionNotNull("columns").size(); ++i) {
            String blockUpdates;
            PropertyList column = maint.getCollection("columns").getPropertyList(i);
            String mode = column.getProperty("mode").trim();
            if (!mode.equalsIgnoreCase("readonly") && !mode.equalsIgnoreCase("hidden")) continue;
            String columnid = column.getProperty("columnid").trim();
            if (columnid.contains(" ")) {
                int pos = columnid.lastIndexOf(" ");
                columnid = columnid.substring(pos + 1);
            }
            if ((blockUpdates = column.getProperty("blockupdates", "N")).equals("A")) {
                blockColumns.append(blockColumns.length() > 0 ? ";" : "").append(StringUtil.replaceAll(columnid, "'", ""));
                continue;
            }
            if (!blockUpdates.equals("Y") || !columnBlockingEnabled) continue;
            blockColumns.append(blockColumns.length() > 0 ? ";" : "").append(StringUtil.replaceAll(columnid, "'", ""));
        }
        return blockColumns.toString();
    }

    public static StringBuffer getPrimaryFKColumnList(PropertyList maint, SDCProcessor sdcProcessor, String sdcid, StringBuffer columnids, StringBuffer customcols) {
        StringBuffer fkcolumnslist = new StringBuffer();
        ArrayList<String> unique = new ArrayList<String>();
        ArrayList<String> fkcolumnids = new ArrayList<String>();
        ArrayList<String> customcolumnids = new ArrayList<String>();
        for (int i = 0; i < maint.getCollection("columns").size(); ++i) {
            String realcol;
            PropertyList link;
            String[] c;
            PropertyList column = maint.getCollection("columns").getPropertyList(i);
            String columnid = column.getProperty("columnid").trim();
            if (columnid.startsWith("(") || columnid.indexOf(".") <= -1 || (c = StringUtil.split(columnid, ".")).length != 2) continue;
            if (c[0].equalsIgnoreCase("trackitem")) {
                if (fkcolumnids != null) {
                    fkcolumnids.add(columnid);
                }
                if (unique.contains(c[0])) continue;
                if (fkcolumnslist.length() > 0) {
                    fkcolumnslist.append(";");
                }
                fkcolumnslist.append(c[0]);
                unique.add(c[0]);
                continue;
            }
            if (c[0].equalsIgnoreCase("sdialias")) {
                if (fkcolumnids != null) {
                    fkcolumnids.add(columnid);
                }
                if (unique.contains(c[0])) continue;
                if (fkcolumnslist.length() > 0) {
                    fkcolumnslist.append(";");
                }
                fkcolumnslist.append(c[0]);
                unique.add(c[0]);
                continue;
            }
            PropertyListCollection links = sdcProcessor.getLinks(sdcid);
            PropertyList propertyList = link = links != null ? links.find("sdccolumnid", c[0]) : null;
            if (link == null || !link.getProperty("linktype").equalsIgnoreCase("F") || column.getProperty("mode").length() <= 0 || column.getProperty("mode").equalsIgnoreCase("text") || column.getProperty("mode").equalsIgnoreCase("retrievedata") || column.getProperty("mode").equalsIgnoreCase("pseudocolumn")) continue;
            if (!unique.contains(c[0])) {
                PropertyList col;
                if (maint.getCollection("columns").find("columnid", c[0]) == null) {
                    col = new PropertyList();
                    col.setProperty("id", "FK_" + c[0]);
                    col.setProperty("columnid", c[0]);
                    col.setProperty("mode", "hidden");
                    maint.getCollection("columns").add(col);
                }
                if (link.getProperty("sdccolumnid2").length() > 0 && maint.getCollection("columns").find("columnid", link.getProperty("sdccolumnid2")) == null) {
                    col = new PropertyList();
                    col.setProperty("id", "FK_" + link.getProperty("sdccolumnid2"));
                    col.setProperty("columnid", link.getProperty("sdccolumnid2"));
                    col.setProperty("mode", "hidden");
                    maint.getCollection("columns").add(col);
                }
                if (fkcolumnslist.length() > 0) {
                    fkcolumnslist.append(";");
                }
                fkcolumnslist.append(c[0]);
                unique.add(c[0]);
            }
            if ((realcol = RequestParser.parseColumn(columnid).substring(c[0].length() + 1)).length() > 0 && sdcProcessor.getColumns(link.getProperty("linksdcid")).find("columnid", realcol) != null) {
                if (fkcolumnids == null) continue;
                fkcolumnids.add(columnid);
                continue;
            }
            if (customcolumnids == null) continue;
            customcolumnids.add(columnid);
        }
        if (columnids != null) {
            for (String columnid : fkcolumnids) {
                if (columnids.length() > 0) {
                    columnids.append(";");
                }
                columnids.append(columnid);
            }
        }
        if (customcols != null) {
            for (String columnid : customcolumnids) {
                if (customcols.length() > 0) {
                    customcols.append(";");
                }
                customcols.append(columnid);
            }
        }
        return fkcolumnslist;
    }

    public Maint(PageContext pageContext, SDITagInfo sdiInfo, String connectionid) {
        this.setPageContext(pageContext);
        this.sdiInfo = sdiInfo;
        this.connectionInfo = new ConnectionProcessor(pageContext).getConnectionInfo(this.getConnectionid());
        this.setConnectionId(connectionid);
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public void setSDCId(String sdcId) {
        this.sdcid = sdcId;
    }

    @Override
    public String getHtml() {
        Trace.setStartCodeBlock("Maint.getHtml()");
        StringBuffer ret = new StringBuffer();
        if (this.datasetName.equals("primary")) {
            this.notes = this.sdiInfo.getDataSet("notes");
            boolean bl = this.showNoteImage = this.notes != null && this.notes.size() > 0;
            if (this.showNoteImage) {
                this.findNotesMap = new HashMap();
            }
        }
        this.rowStatus = this.sdiInfo.getRowStatus(this.datasetName);
        if (this.element == null) {
            return "No element data found for the sdimaint element tag.";
        }
        if (this.sdcid == null || this.sdcid.length() == 0) {
            this.sdcid = this.element.getProperty("sdcid");
        }
        ElementUtil.setSdcPropertyCache(this.pageContext, this.getConnectionId(), this.sdcid, this.sdcid + "_props");
        this.style = MaintStyle.getMaintStyle(this.element.getProperty("style"));
        this.prefix = this.element.getProperty("_prefix");
        this.elementid = this.element.getId();
        PropertyListCollection columns = this.element.getCollection("columns");
        this.requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
        String evalmode = "";
        if (this.element.getProperty("viewonly").length() > 0 && this.element.getProperty("viewonly").indexOf("$G{") == 0) {
            try {
                String viewonly = GroovyUtil.evaluate(this.element.getProperty("viewonly"), this.connectionInfo, this.sdiInfo, this.element, this.requestContext.getPropertyList().getPropertyList("pagedata"), (PropertyList)this.pageContext.getAttribute(this.sdcid + "_props"), this.datasetName);
                viewonly = "true".equals(viewonly) ? "Y" : ("false".equals(viewonly) ? "N" : viewonly);
                this.element.setProperty("viewonly", viewonly);
                if ("Y".equals(viewonly)) {
                    this.requestContext.getPropertyList().getPropertyList("pagedata").setProperty("ViewOnly", "Y");
                }
            }
            catch (SapphireException se) {
                return "<textarea cols=\"50\">" + se.getMessage() + "</textarea>";
            }
        }
        if (columns.size() > 0) {
            String sourcecolumnlist = "";
            for (int i = 0; i < columns.size(); ++i) {
                PropertyList col = columns.getPropertyList(i);
                String cachkey = "";
                cachkey = this.elementid + "_" + col.getProperty("columnid", col.getProperty("id"));
                String cachedmodeexpression = (String)this.pageContext.getAttribute(cachkey);
                String mode = col.getProperty("_evalmode", col.getProperty("mode"));
                String validation = col.getProperty("validation");
                String ddSql = col.getProperty("sql");
                PropertyList dropdowndefinition = col.getPropertyList("dropdowndefinition");
                PropertyList dynamicrenderingPL = col.getPropertyListNotNull("dynamicrendering");
                ArrayList<String> tokensList = new ArrayList<String>();
                ArrayList<String> tokenTypeList = new ArrayList<String>();
                if (dropdowndefinition != null && (dropdowndefinition.getProperty("sdcid").trim().length() > 0 || dropdowndefinition.getProperty("querywhere").trim().length() > 0)) {
                    String queryWhere = dropdowndefinition.getProperty("querywhere");
                    if (queryWhere.length() > 0 && queryWhere.indexOf("[") >= 0 && queryWhere.indexOf("]") > 0) {
                        String[] tokens;
                        for (String t : tokens = StringUtil.getTokens(queryWhere)) {
                            tokensList.add(t);
                            tokenTypeList.add("P");
                        }
                    }
                } else if (ddSql.length() > 0 && ddSql.indexOf("[") >= 0 && ddSql.indexOf("]") > 0) {
                    String[] tokens;
                    for (String t : tokens = StringUtil.getTokens(ddSql)) {
                        tokensList.add(t);
                        tokenTypeList.add("S");
                    }
                }
                try {
                    String[] behaviorpropertyids = new String[]{"mandatoryif", "hiddenif", "readonlyif"};
                    for (int p = 0; p < behaviorpropertyids.length; ++p) {
                        HashSet<String> tokens;
                        String pvalue = dynamicrenderingPL.getProperty(behaviorpropertyids[p]);
                        if (pvalue.length() <= 0 || !pvalue.startsWith("$G{") || (tokens = GroovyUtil.getReferencedProperties(pvalue, this.datasetName)) == null) continue;
                        for (String t : tokens) {
                            tokensList.add(t);
                            tokenTypeList.add("D");
                        }
                    }
                }
                catch (SapphireException se) {
                    return se.getMessage();
                }
                if (tokensList != null && tokensList.size() > 0) {
                    String columnid = col.getProperty("columnid");
                    if (columnid.indexOf(" ") > 0) {
                        columnid = RequestParser.parseAlias(columnid);
                    }
                    HashSet<String> processedSet = new HashSet<String>();
                    for (int t = 0; t < tokensList.size(); ++t) {
                        String token = (String)tokensList.get(t);
                        String type = (String)tokenTypeList.get(t);
                        if (processedSet.contains(token + ";" + type)) continue;
                        processedSet.add(token + ";" + type);
                        if (!"currentuser".equals(token) && !"keyid1list".equals(token)) {
                            if (this.columnDependsMap.get(token) == null) {
                                this.columnDependsMap.put(token, columnid);
                                sourcecolumnlist = sourcecolumnlist + ";" + token;
                            } else {
                                if ((this.columnDependsMap.get(token) + ";").indexOf(";" + columnid + ";") < 0) {
                                    this.columnDependsMap.put(token, this.columnDependsMap.get(token) + ";" + columnid);
                                }
                                if ((sourcecolumnlist + ";").indexOf(";" + token + ";") < 0) {
                                    sourcecolumnlist = sourcecolumnlist + ";" + token;
                                }
                            }
                        }
                        if ("P".equals(type) || "S".equals(type)) {
                            String sqlcode = SQLRegister.registerDynamicSQL("P".equals(type) ? dropdowndefinition : ddSql);
                            this.columnDependsMap.put(columnid + "_sqlcode", sqlcode);
                            continue;
                        }
                        if (!"D".equals(type)) continue;
                        String dcode = MaintAjaxRequest.registerDynamicCode(dynamicrenderingPL);
                        this.columnDependsMap.put(columnid + "_dynamiccode", dcode);
                    }
                }
                if (validation.indexOf("$G{") == 0 && this.sdiInfo.getCurrentRow(this.datasetName) == 0) {
                    try {
                        validation = GroovyUtil.evaluate(validation, this.connectionInfo, this.sdiInfo, this.element, this.requestContext.getPropertyList().getPropertyList("pagedata"), (PropertyList)this.pageContext.getAttribute(this.sdcid + "_props"), this.datasetName);
                        col.setProperty("validation", validation);
                    }
                    catch (Exception e) {
                        throw new RuntimeException("Fail to evaluation expression:" + validation);
                    }
                }
                if (mode.indexOf("$G{") != 0 && cachedmodeexpression == null) continue;
                if (this.pageContext.getAttribute(cachkey) == null) {
                    this.pageContext.setAttribute(cachkey, (Object)mode);
                    this.pageContext.setAttribute(cachkey + "_modeset", new HashSet());
                }
                try {
                    evalmode = this.datasetName == null || this.datasetName.length() == 0 || this.datasetName.equalsIgnoreCase("primary") ? GroovyUtil.evaluate((String)this.pageContext.getAttribute(cachkey), this.connectionInfo, this.sdiInfo, this.element, this.requestContext.getPropertyList().getPropertyList("pagedata"), (PropertyList)this.pageContext.getAttribute(this.sdcid + "_props")) : GroovyUtil.evaluate((String)this.pageContext.getAttribute(cachkey), this.connectionInfo, this.sdiInfo, this.element, this.requestContext.getPropertyList().getPropertyList("pagedata"), (PropertyList)this.pageContext.getAttribute(this.sdcid + "_props"), this.datasetName);
                }
                catch (SapphireException se) {
                    return "<textarea cols=\"50\">" + se.getMessage() + "</textarea>";
                }
                HashSet set = (HashSet)this.pageContext.getAttribute(cachkey + "_modeset");
                set.add(evalmode);
                if (set.size() > 2 || set.size() == 2 && !set.contains("readonly") && !set.contains("maskvalue")) {
                    return "Invalid mode combination returned for column mode expression:" + this.pageContext.getAttribute(cachkey + " modes:" + set.toString());
                }
                if (this.style.isGrid()) {
                    if (!col.containsKey("_evalmode")) {
                        col.setProperty("_evalmode", mode);
                    }
                    col.setProperty("mode", evalmode);
                    if (this.sdiInfo.getCurrentRow(this.datasetName) != 0 || !"retrievedata".equals(evalmode) && !"hidden".equals(evalmode)) continue;
                    this.pageContext.removeAttribute(cachkey);
                    continue;
                }
                if (!col.containsKey("_evalmode")) {
                    col.setProperty("_evalmode", mode);
                }
                col.setProperty("mode", evalmode);
            }
            if (sourcecolumnlist.length() > 0) {
                this.columnDependsMap.put("sourcecolumnlist", sourcecolumnlist.substring(1));
            }
        } else {
            ret.append(this.getTranslationProcessor().translate("No columns defined"));
        }
        if (this.sdiInfo.getCurrentRow(this.datasetName) == 0) {
            ElementUtil.setColumnDateDisplayFormat(this.pageContext, this.element.getCollection("columns"), this.datasetName, this.sdiInfo, (PropertyList)this.pageContext.getAttribute(this.sdcid + "_props"));
            ElementUtil.setColumnDisplayValue(this.pageContext, this.element.getCollection("columns"), (PropertyList)this.pageContext.getAttribute(this.sdcid + "_props"), this.getTranslationProcessor());
            PropertyListCollection maintColumns = this.element.getCollection("columns");
            PropertyList sdcProps = (PropertyList)this.pageContext.getAttribute(this.sdcid + "_props");
            if (maintColumns != null && sdcProps != null && sdcProps.getCollection("columns") != null) {
                ElementUtil.setColumnDefaultTitle(maintColumns, sdcProps.getCollection("columns"), this.getTranslationProcessor());
            }
        }
        if (this.elementid.equalsIgnoreCase("maint")) {
            ret.append("<script type=\"text/javascript\">_lvtop.sapphire.page.maint.style='").append(this.style.toString().toLowerCase()).append("';_lvtop.sapphire.page.maint.compcode='").append(Configuration.getCompcode(this.connectionInfo.getDatabaseId())).append("';</script>");
        }
        if (this.element.getProperty("dynamicaudit").length() > 0) {
            ret.append("<script type=\"text/javascript\">sapphire.page.data.dynamicaudit_" + this.elementid + "='" + this.element.getProperty("dynamicaudit") + "';</script>");
        }
        if (this.style.isGrid()) {
            ret.append(this.getGridHtml());
        } else {
            ret.append(this.getFormHtml());
        }
        if (this.columnDependsMap.get("sourcecolumnlist") != null && this.columnDependsMap.get("sourcecolumnlist").length() > 0 || this.fieldInitialbehaviorMap.size() > 0) {
            ret.append("<script type=\"text/javascript\">");
            if (this.columnDependsMap.get("sourcecolumnlist") != null && this.columnDependsMap.get("sourcecolumnlist").length() > 0) {
                ret.append("\nsapphire.page.data.sourcecolumnlist_" + this.elementid + "='" + this.columnDependsMap.get("sourcecolumnlist") + "';");
            }
            if ("primary".equals(this.datasetName) && this.sdiInfo.getCurrentRow(this.datasetName) >= 0) {
                if (this.sdiInfo.getCurrentRow(this.datasetName) == 0) {
                    HashMap<String, String> cloneMap = new HashMap<String, String>();
                    for (String key : this.fieldInitialbehaviorMap.keySet()) {
                        String value = this.fieldInitialbehaviorMap.get(key);
                        String rowkey = StringUtil.replaceAll(key, "0", "_row");
                        cloneMap.put(rowkey, value);
                    }
                    ret.append("\nsapphire.page.data.initialbehavior_" + this.elementid + "_row=" + new PropertyList(cloneMap).toJSONString(false, false) + ";");
                    for (int i = 0; i < columns.size(); ++i) {
                        PropertyList column = columns.getPropertyList(i);
                        String columnId = column.getProperty("columnid");
                        String defaultValue = column.getProperty("defaultvalue");
                        if (defaultValue.length() <= 0) continue;
                        this.fieldDefaultValueMap.put(columnId, ElementUtil.evaluateExpression(0, columnId, column.getProperty("defaultvalue"), this.sdiInfo));
                    }
                    ret.append("\nsapphire.page.data.defaultColumnValue_" + this.elementid + "_row=" + new PropertyList(this.fieldDefaultValueMap).toJSONString(false, false) + ";");
                }
                ret.append("\nsapphire.page.data.initialbehavior_" + this.elementid + "_" + this.sdiInfo.getCurrentRow(this.datasetName) + "=" + new PropertyList(this.fieldInitialbehaviorMap).toJSONString(false, false) + ";");
                ret.append("\nsapphire.page.data.initialbehavior_" + this.elementid + "_rows=" + (this.sdiInfo.getCurrentRow(this.datasetName) + 1) + ";");
            }
            ret.append("\n</script>");
        }
        Trace.setEndCodeBlock("Maint.getHtml()");
        return ret.toString();
    }

    private String getFormHtml() {
        boolean showLockedImage;
        StringBuffer html = new StringBuffer();
        html.append(this.getCommonJsIncludes());
        String cssclass = "maintform_field";
        String lockedby = this.sdiInfo.getValue(this.datasetName, "__lockedby");
        String checkedoutby = this.sdiInfo.getValue(this.datasetName, "__checkedoutbyuser");
        String checkedoutbydept = this.sdiInfo.getValue(this.datasetName, "__checkedoutbydepartment");
        LockedImage lockedImageObj = LockedImage.getLockedImage(lockedby, checkedoutby, checkedoutbydept, this.connectionInfo, this.getTranslationProcessor());
        boolean isLocked = lockedImageObj.isLocked();
        boolean isCheckedOut = lockedImageObj.isCheckedOut();
        String lockedImage = lockedImageObj.getLockedImage();
        boolean bl = showLockedImage = isLocked && lockedby.length() > 0 || isLocked && "primary".equals(this.datasetName) || isCheckedOut && "primary".equals(this.datasetName);
        if (isLocked) {
            cssclass = "maint_lockedfield";
            html.append("<script>");
            html.append("var __parentSDILocked = true;");
            html.append("var __parentSDILockedBy = '").append(lockedby).append("';");
            html.append("</script>");
            this.pageContext.setAttribute("__parentSDILocked", (Object)"Y");
        } else {
            html.append("<script>");
            html.append("var __parentSDILocked = false;");
            html.append("var __parentSDILockedBy = '';");
            html.append("</script>");
        }
        if (this.pageContext.getAttribute("jsincluded") == null || !this.pageContext.getAttribute("jsincluded").equals("Y")) {
            html.append(this.getGridJsIncludes());
            this.pageContext.setAttribute("jsincluded", (Object)"Y");
        }
        PropertyListCollection columns = this.element.getCollection("columns");
        int currentRow = this.sdiInfo.getCurrentRow(this.datasetName);
        if (columns != null && columns.size() > 0) {
            int formCols = 1;
            try {
                formCols = Integer.parseInt(this.element.getProperty("formcols"));
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
            PropertyList sdcProps = (PropertyList)this.pageContext.getAttribute(this.sdcid + "_props");
            if (sdcProps == null) {
                sdcProps = new PropertyList();
            }
            ArrayList tabGroups = Maint.getGroups(this.style, columns, StringUtil.initCaps(sdcProps.getProperty("singular")));
            for (int i = 0; i < tabGroups.size(); ++i) {
                String groupid = (String)tabGroups.get(i);
                MaintColumn maintColumn = new MaintColumn(this.pageContext, this.sdiInfo, this.getConnectionId());
                maintColumn.setElementProperties(this.element);
                maintColumn.setRowStatus(this.rowStatus);
                maintColumn.setLocked(isLocked);
                maintColumn.setDatasetname(this.datasetName);
                maintColumn.setSdcPropertyList(sdcProps);
                maintColumn.setColumnDependsMap(this.columnDependsMap);
                html.append("<table id=\"mainttable_").append(groupid).append("\" class=\"maintform_table\" cellpadding=\"3\" cellspacing=\"0\" style=\"width:100%;display:").append(i == 0 || this.style.hasFieldGroups() ? (this.browser.isIE() && this.browser.getVersion() < 8.0 ? "block" : "table") : "none").append(";").append(this.browser.isSafari() ? "margin-left:-1px;margin-bottom:1px;" : "").append(this.style.hasFieldGroups() && i > 0 ? "margin-top:20px;" : "").append("\">\n");
                if (this.style.hasFieldGroups()) {
                    html.append("").append("<tr height=\"8\"><td class=\"maintform_fieldtitle\" colspan=\"").append(formCols * 2).append("\">").append(this.getTranslationProcessor().translate(groupid)).append("</td></tr>\n");
                }
                html.append("<tr height=\"8\">\n");
                int colPos = 0;
                boolean sizeadapterRendered = false;
                for (int j = 0; j < columns.size(); ++j) {
                    boolean mandatory;
                    boolean forceStartAtColumn1;
                    int span;
                    PropertyList column = columns.getPropertyList(j);
                    if (column.getProperty("columnid").length() == 0 && column.getProperty("pseudocolumn").length() == 0 && column.getProperty("title").length() == 0 || !groupid.equals(column.getProperty("groupid")) || column.getProperty("mode").equals("retrievedata")) continue;
                    maintColumn.setColumn(column);
                    PropertyList inputAttributes = maintColumn.getInputAttributes();
                    maintColumn.setInputAttributes(inputAttributes);
                    String currentValue = this.sdiInfo.getDataSet(this.datasetName).getValue(currentRow, inputAttributes.getProperty("columnid"), "");
                    if ("I".equals(this.rowStatus) && column.getProperty("defaultvalue").length() > 0 && currentValue.length() == 0) {
                        String columnid = inputAttributes.getProperty("columnid");
                        this.sdiInfo.getDataSet(this.datasetName).setValue(currentRow, columnid, ElementUtil.evaluateExpression(currentRow, columnid, column.getProperty("defaultvalue"), this.sdiInfo));
                    }
                    if (column.getProperty("mode").equals("hidden")) {
                        html.append(maintColumn.getHtml());
                        continue;
                    }
                    try {
                        span = Integer.parseInt(inputAttributes.getProperty("span"));
                    }
                    catch (NumberFormatException nfe) {
                        span = 1;
                    }
                    boolean bl2 = forceStartAtColumn1 = "Y".equals(column.getProperty("forcenewrow")) || span < 0 && colPos != 0;
                    if (span < 0) {
                        span = -1 * span;
                    }
                    if ((colPos += span) > formCols || forceStartAtColumn1 && colPos > 1) {
                        html.append("<td nowrap class=\"maintform_field\" colspan=\"").append(String.valueOf((formCols - colPos + span) * 2)).append("\">&nbsp;</td>\n</tr>\n");
                        if (j < columns.size()) {
                            html.append("<tr>\n");
                        }
                        colPos = span;
                    }
                    PropertyList link = column.getPropertyList("link");
                    String title = inputAttributes.getProperty("title");
                    String validationProperty = inputAttributes.getProperty("validation");
                    boolean bl3 = mandatory = validationProperty.contains("Mandatory") || validationProperty.contains("mandatory");
                    if (title != null && !title.equalsIgnoreCase("[-]")) {
                        if (link != null && link.getProperty("href").length() > 0) {
                            html.append("<td class=\"maintform_fieldtitle\">");
                            html.append(ElementUtil.getLink(this.datasetName, column.getProperty("columnid"), this.sdiInfo, link, inputAttributes.getProperty("title"), -1, true, "Y".equals(column.getProperty("translatevalue")) ? this.getTranslationProcessor() : null, true));
                            html.append("</td>\n");
                        } else {
                            html.append("<td class=\"maintform_fieldtitle\">").append(inputAttributes.getProperty("title")).append(mandatory ? " <span style=\"color:red;font-weight:600\">*</span>" : "").append("</td>\n");
                        }
                        html.append("<td nowrap class=\"").append(cssclass).append("\" colspan=\"").append(String.valueOf(2 * (span - 1) + 1)).append("\">").append(maintColumn.getHtml()).append(showLockedImage ? lockedImage : "");
                        if (!sizeadapterRendered && this.browser.isPhone()) {
                            html.append("<div id=\"maintfield_sizeadapter\" style=\"height:2px;width:100px;\"></div>");
                            sizeadapterRendered = true;
                        }
                        html.append("</td>\n");
                    } else {
                        html.append("<td nowrap class=\"").append(cssclass).append("\" colspan=\"").append(String.valueOf(2 * (span - 1) + 2)).append("\">").append(maintColumn.getHtml()).append(showLockedImage ? lockedImage : "").append("</td>\n");
                    }
                    showLockedImage = false;
                    if (colPos % formCols == 0) {
                        html.append("</tr>\n");
                        if (j < columns.size() - 1) {
                            html.append("<tr>\n");
                        }
                    }
                    colPos %= formCols;
                    if (inputAttributes.getProperty("initialbehavior").length() <= 0) continue;
                    this.fieldInitialbehaviorMap.put(inputAttributes.getProperty("name"), inputAttributes.getProperty("initialbehavior"));
                }
                if (colPos != 0) {
                    html.append("<td nowrap class=\"maintform_field\" colspan=\"").append(String.valueOf(2 * (formCols - colPos))).append("\">&nbsp;</td></tr>\n");
                }
                html.append("</table>\n");
            }
        }
        if (currentRow == this.sdiInfo.getRowCount(this.datasetName) - 1) {
            if (this.element.getProperty("templateflag").equals("Y")) {
                html.append("<input type=\"hidden\" name=\"__").append(this.prefix).append("pr_extraprops\" id=\"__").append(this.prefix).append("pr_extraprops\" value=\"templateflag=Y\"/>");
            }
            html.append("<input type=\"hidden\" name=\"__pr_extraprops\"  id=\"__pr_extraprops\" value=\"\"/>");
            html.append(this.getValidationScript(this.element.getCollection("columns"), this.pageContext));
            html.append("<script>\n");
            html.append("\nelementprefix['").append(this.elementid).append("']='").append(this.prefix).append("';");
            html.append(this.pageContext.getAttribute("dd_dropdownvalues") != null ? this.pageContext.getAttribute("dd_dropdownvalues") : "");
            html.append(this.pageContext.getAttribute("dd_dropdownfields") != null ? this.pageContext.getAttribute("dd_dropdownfields") : "");
            html.append("</script>");
        }
        return html.toString();
    }

    private String getGridHtml() {
        boolean showLockedImage;
        String cssclass = "gridmaint_field";
        String lockedby = this.sdiInfo.getValue(this.datasetName, "__lockedby");
        String checkedoutby = this.sdiInfo.getValue(this.datasetName, "__checkedoutbyuser");
        String checkedoutbydept = this.sdiInfo.getValue(this.datasetName, "__checkedoutbydepartment");
        LockedImage lockedImageObj = LockedImage.getLockedImage(lockedby, checkedoutby, checkedoutbydept, this.connectionInfo, this.getTranslationProcessor());
        boolean isLocked = lockedImageObj.isLocked();
        boolean isCheckedOut = lockedImageObj.isCheckedOut();
        String lockedImage = lockedImageObj.getLockedImage();
        boolean bl = showLockedImage = isLocked && lockedby.length() > 0 || isLocked && "primary".equals(this.datasetName) || isCheckedOut && "primary".equals(this.datasetName);
        if (isLocked) {
            cssclass = "maint_lockedfield";
        }
        boolean isOldScroll = false;
        boolean isNewScroll = false;
        boolean tbodyout = false;
        boolean isScrollWithCheckBox = false;
        int fixedcol = 0;
        try {
            if (this.element.getProperty("fixedcols").length() > 0) {
                fixedcol = Integer.parseInt(this.element.getProperty("fixedcols"));
            }
            if (fixedcol > 0) {
                isNewScroll = true;
                isOldScroll = false;
                MaintStyle style = MaintStyle.getMaintStyle(this.element.getProperty("style"));
                if (style.hasCheckbox()) {
                    isScrollWithCheckBox = true;
                }
            }
        }
        catch (Exception style) {
            // empty catch block
        }
        PropertyList sdcProps = (PropertyList)this.pageContext.getAttribute(this.sdcid + "_props");
        if (sdcProps == null) {
            sdcProps = new PropertyList();
        }
        StringBuffer html = new StringBuffer();
        if (this.pageContext.getAttribute("jsincluded") == null || !this.pageContext.getAttribute("jsincluded").equals("Y")) {
            html.append(this.getGridJsIncludes());
            if (!isOldScroll && isNewScroll) {
                PropertyListCollection plugins = new PropertyListCollection();
                PropertyList plugin = new PropertyList();
                plugin.setProperty("pluginid", "tableHeadFixer");
                plugin.setProperty("css", "N");
                plugin.setProperty("allowminimized", "Y");
                plugins.add(plugin);
                ConfigurationProcessor config = new ConfigurationProcessor(this.pageContext);
                boolean devMode = false;
                try {
                    devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
                }
                catch (Exception e) {
                    devMode = false;
                }
                html.append(JavaScriptAPITag.getJQueryAPI(true, false, plugins, "", !devMode, this.pageContext));
            }
            this.pageContext.setAttribute("jsincluded", (Object)"Y");
        }
        if (!isOldScroll && isNewScroll) {
            html.append("<script>");
            html.append("$( window ).on('load',function(){newScrollGrid.setUp('").append(this.elementid).append("',").append(isScrollWithCheckBox ? fixedcol + 1 : fixedcol).append(")});");
            html.append("</script>");
        }
        PropertyListCollection columns = this.element.getCollection("columns");
        if (this.pageContext.getAttribute(this.elementid + "idGrid") == null) {
            ArrayList idGrid = new ArrayList();
            this.pageContext.setAttribute(this.elementid + "idGrid", idGrid);
        }
        int currentRow = this.sdiInfo.getCurrentRow(this.datasetName);
        String currentRowId = this.sdiInfo.getRowId(this.datasetName);
        if (currentRowId == null || currentRowId.length() == 0) {
            currentRowId = "" + currentRow;
        }
        if (columns != null && columns.size() > 0) {
            String cellCssClass;
            ArrayList tabGroups = Maint.getGroups(this.style, columns, StringUtil.initCaps(sdcProps.getProperty("singular")));
            MaintColumn maintColumn = new MaintColumn(this.pageContext, this.sdiInfo, this.getConnectionId());
            maintColumn.setElementProperties(this.element);
            maintColumn.setRowStatus(this.rowStatus);
            maintColumn.setLocked(isLocked);
            maintColumn.setDatasetname(this.datasetName);
            maintColumn.setSdcPropertyList((PropertyList)this.pageContext.getAttribute(this.sdcid + "_props"));
            maintColumn.setColumnDependsMap(this.columnDependsMap);
            SmartScrollGridRenderer grid = (SmartScrollGridRenderer)this.pageContext.getAttribute(this.elementid + "_smartscrollgrid");
            if (grid == null) {
                grid = new SmartScrollGridRenderer(this.elementid, this.browser);
                this.pageContext.setAttribute(this.elementid + "_smartscrollgrid", (Object)grid);
            }
            String cell = "td";
            if (isNewScroll) {
                cell = "th";
            }
            if (currentRow == 0 || currentRow == -9998) {
                this.pageContext.setAttribute(this.elementid + "_columnidlist", (Object)new StringBuffer());
                if (!isOldScroll) {
                    if (isNewScroll) {
                        html.append("<div id=\"").append(this.elementid).append("_wrapper\" style=\"").append(isNewScroll ? "width:100px;height:auto;padding-bottom:15px;overflow-x:hidden;flex: 1 1 auto;" : "").append("position: relative;\">");
                    }
                    html.append("<table id=\"").append(this.elementid).append("_table\" class=\"gridmaint_table\" cellspacing=\"0\">");
                    if (isNewScroll) {
                        html.append("<thead>");
                    }
                    html.append("<tr class=\"gridmaint_tablehead\">");
                    if (this.style.hasCheckbox()) {
                        html.append("<").append(cell).append(" nowrap class=\"gridmaint_fieldtitle\"><input type=\"checkbox\" onclick=\"checkAllDetail( this, '").append(this.element.getId()).append("' )\" id=\"").append(this.element.getId()).append("_selectAll\"/></").append(cell).append(">");
                    }
                }
                int fixcolrendered = 0;
                StringBuffer bufferedHeader = new StringBuffer();
                int colIndex = 0;
                for (int i = 0; i < columns.size(); ++i) {
                    PropertyList column = columns.getPropertyList(i);
                    if (column.getProperty("columnid").length() == 0 && column.getProperty("pseudocolumn").length() == 0 && column.getProperty("title").length() == 0) continue;
                    String groupid = "";
                    if (this.style.hasTabGroups()) {
                        groupid = column.getProperty("groupid").length() > 0 ? column.getProperty("groupid") : "default";
                    }
                    if (i == 0 && isScrollWithCheckBox) {
                        grid.addTopLeftCell("<input type=\"checkbox\" onclick=\"checkAllDetail( this, '" + this.element.getId() + "' )\" id=\"" + this.element.getId() + "_selectAll\"/>", "gridmaint_fieldtitle", true, 30);
                    }
                    if (!column.getProperty("mode").equals("hidden") && !column.getProperty("mode").equals("retrievedata")) {
                        String title = column.getProperty("title");
                        String columnid = column.getProperty("columnid");
                        cellCssClass = column.getProperty("cellcssclass");
                        if (cellCssClass != null) {
                            cellCssClass = cellCssClass.trim();
                        }
                        if (columnid.indexOf(" ") > 0) {
                            columnid = RequestParser.parseAlias(columnid);
                        }
                        String string = title = title == null || title.trim().length() == 0 ? columnid : title;
                        if (!isOldScroll) {
                            boolean isPseudoFromLink;
                            html.append("<").append(cell).append(" nowrap class=\"gridmaint_fieldtitle");
                            if (column.getProperty("mode").equalsIgnoreCase("checkbox")) {
                                html.append(" ").append("gridmaint_ftsmall");
                            } else if (column.getProperty("mode").equalsIgnoreCase("formattedtext")) {
                                html.append(" ").append("gridmaint_ftlarge");
                            } else {
                                html.append(" ").append("gridmaint_ftmed");
                            }
                            if (cellCssClass.length() > 0) {
                                html.append(" ").append(cellCssClass.trim());
                            }
                            html.append("\" id=\"");
                            html.append("__").append(this.prefix).append(SDIData.getDatasetCode(this.datasetName)).append("_").append(columnid);
                            html.append("\"");
                            if (isNewScroll) {
                                if (fixcolrendered >= fixedcol && groupid.length() > 0) {
                                    html.append(" data-groupid=\"").append(groupid).append("\"");
                                    if (tabGroups.size() > 0 && !tabGroups.get(0).equals(groupid)) {
                                        html.append(" style=\"display:none;\"");
                                    }
                                }
                            } else if (groupid.length() > 0) {
                                html.append(" data-groupid=\"").append(groupid).append("\"");
                                if (tabGroups.size() > 0 && !tabGroups.get(0).equals(groupid)) {
                                    html.append(" style=\"display:none;\"");
                                }
                            }
                            boolean isPseudo = column.getProperty("pseudocolumn").length() > 0;
                            boolean bl2 = isPseudoFromLink = column.getPropertyList("link") != null && column.getPropertyList("link").getProperty("href").trim().length() > 0 && column.getProperty("mode").equals("readonly");
                            if (!isPseudo && !isPseudoFromLink) {
                                html.append(" title=\"" + this.getTranslationProcessor().translate("Click to select column.") + "\"  onclick=\"getHandler('" + this.elementid + "').selectColumn( " + colIndex++ + " );\"");
                            }
                            html.append(">").append(title).append("</").append(cell).append(">\n");
                            if (isNewScroll && fixcolrendered < fixedcol) {
                                ++fixcolrendered;
                            }
                        } else if (fixcolrendered < fixedcol) {
                            if (cellCssClass.length() > 0) {
                                grid.addTopLeftCell(title, "gridmaint_fieldtitle " + cellCssClass, true);
                            } else {
                                grid.addTopLeftCell(title, "gridmaint_fieldtitle", true);
                            }
                            ++fixcolrendered;
                        } else if (cellCssClass.length() > 0) {
                            grid.addTopRightCell(title, "gridmaint_fieldtitle " + cellCssClass, "", false);
                        } else {
                            grid.addTopRightCell(title, "gridmaint_fieldtitle", "", false);
                        }
                        ((StringBuffer)this.pageContext.getAttribute(this.elementid + "_columnidlist")).append(";").append(columnid);
                        continue;
                    }
                    if (column.getProperty("mode").equals("retrievedata")) continue;
                    StringBuffer touse = isNewScroll ? bufferedHeader : html;
                    touse.append("<").append(cell).append(" style=\"display:none;width:1px;\"></").append(cell).append(">");
                }
                html.append(bufferedHeader);
                if (!isOldScroll) {
                    html.append("</tr>");
                    if (isNewScroll) {
                        html.append("</thead>");
                    }
                    if (this.sdiInfo.getRowCount(this.datasetName) == 0) {
                        html.append("</table>");
                    }
                }
            }
            if (currentRow != -9998) {
                if (!isOldScroll) {
                    if (!tbodyout && isNewScroll) {
                        html.append("<tbody>");
                    }
                    html.append("<tr valign=\"top\" height=\"8\" id=\"__").append(this.prefix).append(SDIData.getDatasetCode(this.datasetName)).append(currentRow == -9999 ? "[__row]" : currentRowId).append("_row\">\n");
                    if (this.style.hasCheckbox()) {
                        if (isLocked) {
                            html.append("<td nowrap class=\"").append(cssclass).append("\">&nbsp;</td>");
                        } else {
                            String id = "__" + this.prefix + SDIData.getDatasetCode(this.datasetName) + (currentRow == -9999 ? "[__row]" : currentRowId);
                            html.append("<td nowrap class=\"gridmaint_field\"").append(isNewScroll ? " style=\"z-index:1;\"" : "").append("><input type=\"checkbox\" name=\"").append(this.element.getId()).append("_selector\" id=\"").append(id).append("\" value=\"").append(id).append("\"/></td>");
                        }
                    }
                }
                ArrayList<String> idRow = new ArrayList<String>();
                boolean isNewRow = true;
                boolean isNewScrollRow = true;
                boolean hasNotes = false;
                if (this.showNoteImage) {
                    this.findNotesMap.put("sdcid", this.sdiInfo.getSdcid());
                    this.findNotesMap.put("keyid1", this.sdiInfo.getDataSet("primary").getValue(currentRow, sdcProps.getProperty("keycolid1"), ""));
                    this.findNotesMap.put("keyid2", this.sdiInfo.getDataSet("primary").getValue(currentRow, sdcProps.getProperty("keycolid2"), "(null)"));
                    this.findNotesMap.put("keyid3", this.sdiInfo.getDataSet("primary").getValue(currentRow, sdcProps.getProperty("keycolid3"), "(null)"));
                    hasNotes = this.notes.findRow(this.findNotesMap) >= 0;
                }
                int fixcolrendered = 0;
                StringBuffer bufferedCells = new StringBuffer();
                for (int i = 0; i < columns.size(); ++i) {
                    PropertyList column = columns.getPropertyList(i);
                    cellCssClass = column.getProperty("cellcssclass");
                    if (cellCssClass != null) {
                        cellCssClass = cellCssClass.trim();
                    }
                    if (column.getProperty("mode").equals("retrievedata")) continue;
                    maintColumn.setColumn(column);
                    PropertyList inputAttributes = maintColumn.getInputAttributes();
                    maintColumn.setInputAttributes(inputAttributes);
                    String currentValue = this.sdiInfo.getDataSet(this.datasetName).getValue(currentRow, column.getProperty("columnid"), "");
                    if ("I".equals(this.rowStatus) && column.getProperty("defaultvalue").length() > 0 && currentValue.length() == 0 && currentRow != -9999) {
                        String columnid = column.getProperty("columnid");
                        if (columnid.indexOf(" ") > 0) {
                            columnid = RequestParser.parseAlias(columnid);
                        }
                        this.sdiInfo.getDataSet(this.datasetName).setValue(currentRow, columnid, ElementUtil.evaluateExpression(currentRow, columnid, column.getProperty("defaultvalue"), this.sdiInfo));
                    }
                    if (column.getProperty("mode").equals("hidden")) {
                        if (!isOldScroll) {
                            StringBuffer touse = isNewScroll ? bufferedCells : html;
                            touse.append("<td style=\"display:none;width:1px;\">");
                            touse.append(maintColumn.getHtml());
                            touse.append("</td>");
                        } else {
                            html.append(maintColumn.getHtml());
                        }
                    } else {
                        String titleclass = "gridmaint_fieldtitle";
                        String groupid = "";
                        if (this.style.hasTabGroups()) {
                            groupid = column.getProperty("groupid").length() > 0 ? column.getProperty("groupid") : "default";
                        }
                        if (isLocked) {
                            titleclass = cssclass;
                        }
                        if (!isOldScroll) {
                            html.append("<td nowrap class=\"").append(cssclass);
                            if (cellCssClass.length() > 0) {
                                html.append(" ").append(cellCssClass);
                            }
                            html.append("\"");
                            if (isNewScroll) {
                                if (fixcolrendered < fixedcol) {
                                    html.append(" style=\"z-index:1;\"");
                                } else if (groupid.length() > 0) {
                                    html.append(" data-groupid=\"").append(groupid).append("\"");
                                    if (tabGroups.size() > 0 && !tabGroups.get(0).equals(groupid)) {
                                        html.append(" style=\"display:none;\"");
                                    }
                                }
                            } else if (groupid.length() > 0) {
                                html.append(" data-groupid=\"").append(groupid).append("\"");
                                if (tabGroups.size() > 0 && !tabGroups.get(0).equals(groupid)) {
                                    html.append(" style=\"display:none;\"");
                                }
                            }
                            html.append(">").append(maintColumn.getHtml()).append(showLockedImage ? lockedImage : "").append("</td>\n");
                            showLockedImage = false;
                            if (isNewScroll && fixcolrendered < fixedcol) {
                                ++fixcolrendered;
                            }
                        } else if (fixcolrendered < fixedcol) {
                            if (isNewRow) {
                                if (isScrollWithCheckBox) {
                                    if (isLocked) {
                                        grid.addBottomLeftCell("&nbsp;", titleclass, true);
                                    } else {
                                        grid.addBottomLeftCell("<input type=\"checkbox\" name=\"" + this.element.getId() + "_selector\" id=\"__" + this.prefix + SDIData.getDatasetCode(this.datasetName) + this.sdiInfo.getCurrentRow(this.datasetName) + "\"/>" + (this.showNoteImage ? "<a href=\"#\" title=\"Show notes for this " + sdcProps.getProperty("singular").toLowerCase() + "\" ><img src=\"WEB-CORE/imageref/finance_business_and_trade/office/notes/16/note.png\" style=\"border:none;margin-bottom:-2px" + (hasNotes ? "" : ";display:none") + "\" id=\"sdinote_" + this.sdiInfo.getCurrentRow(this.datasetName) + "\"/></a>" : ""), titleclass, true);
                                    }
                                    if (cellCssClass.length() > 0) {
                                        grid.addBottomLeftCell(maintColumn.getHtml() + lockedImage, titleclass + " " + cellCssClass, false);
                                    } else {
                                        grid.addBottomLeftCell(maintColumn.getHtml() + lockedImage, titleclass, false);
                                    }
                                } else if (cellCssClass.length() > 0) {
                                    grid.addBottomLeftCell(maintColumn.getHtml() + lockedImage, titleclass + " " + cellCssClass, true);
                                } else {
                                    grid.addBottomLeftCell(maintColumn.getHtml() + lockedImage, titleclass, true);
                                }
                                isNewRow = false;
                            } else if (cellCssClass.length() > 0) {
                                grid.addBottomLeftCell(maintColumn.getHtml(), titleclass + " " + cellCssClass, false);
                            } else {
                                grid.addBottomLeftCell(maintColumn.getHtml(), titleclass, false);
                            }
                            ++fixcolrendered;
                        } else if (isNewScrollRow) {
                            if (cellCssClass.length() > 0) {
                                grid.addBottomRightCell(maintColumn.getHtml(), cssclass + " " + cellCssClass, true);
                            } else {
                                grid.addBottomRightCell(maintColumn.getHtml(), cssclass, true);
                            }
                            isNewScrollRow = false;
                        } else if (cellCssClass.length() > 0) {
                            grid.addBottomRightCell(maintColumn.getHtml(), cssclass + " " + cellCssClass, false);
                        } else {
                            grid.addBottomRightCell(maintColumn.getHtml(), cssclass, false);
                        }
                    }
                    String mode = column.getProperty("mode");
                    String pseudocolumn = column.getProperty("pseudocolumn");
                    if (!(mode.equals("hidden") || pseudocolumn != null && pseudocolumn.length() > 0)) {
                        if (mode.equals("checkbox")) {
                            idRow.add(maintColumn.getId() + "_chx");
                        } else if (mode.indexOf("radiobutton") >= 0) {
                            idRow.add(maintColumn.getId() + "_radio");
                        } else if (mode.equals("readonly") && (column.getProperty("displayvalue").length() > 0 || "Y".equals(column.getProperty("translatevalue")) || maintColumn.getColumn() != null && maintColumn.getColumn().getProperty("reftypeid").length() > 0 && !"Y".equals(column.getProperty("translatevalue")))) {
                            idRow.add(maintColumn.getId() + "__dv");
                        } else {
                            idRow.add(maintColumn.getId());
                        }
                    }
                    if (inputAttributes.getProperty("initialbehavior").length() <= 0) continue;
                    this.fieldInitialbehaviorMap.put(inputAttributes.getProperty("name"), inputAttributes.getProperty("initialbehavior"));
                }
                html.append(bufferedCells);
                if (!isOldScroll) {
                    html.append("</tr>\n");
                }
                ((ArrayList)this.pageContext.getAttribute(this.elementid + "idGrid")).add(idRow);
                if (currentRow == this.sdiInfo.getRowCount(this.datasetName) - 1) {
                    if (!isOldScroll) {
                        if (!tbodyout && isNewScroll) {
                            html.append("</tbody>");
                            tbodyout = true;
                        }
                        html.append("</table>");
                        if (isNewScroll) {
                            html.append("</div>");
                        }
                    } else {
                        grid.renderGrid(html);
                    }
                    if (this.element.getProperty("templateflag").equals("Y")) {
                        html.append("<input type=\"hidden\" name=\"__").append(this.prefix).append("pr_extraprops\" id=\"__").append(this.prefix).append("pr_extraprops\" value=\"templateflag=Y\"/>");
                    }
                    html.append(SDITagUtil.getGrid((ArrayList)this.pageContext.getAttribute(this.elementid + "idGrid"), this.elementid, this.browser, this.getTranslationProcessor()));
                    html.append("\n<script>");
                    if (isOldScroll) {
                        html.append(this.elementid).append("handler1.setContainingdiv( '").append(this.elementid).append("br_div' )");
                    }
                    html.append("\nelementprefix['").append(this.elementid).append("']='").append(this.prefix).append("';");
                    html.append("\ncellsmap['").append(this.elementid).append("']=__").append(this.elementid).append("cells;");
                    html.append("\nhandlermap['").append(this.elementid).append("']=").append(this.elementid).append("handler1;");
                    html.append(this.getColumnidlistJs());
                    if (this.pageContext.getAttribute(this.elementid + "_columnidlist") != null) {
                        html.append("\ncolumnlistmap['").append(this.elementid).append("_columnidlist']='").append(this.pageContext.getAttribute(this.elementid + "_columnidlist").toString().substring(1)).append("';");
                    }
                    html.append("\nscrollgridspecmap['").append(this.elementid).append("rightmargin']=30;");
                    html.append("\nscrollgridspecmap['").append(this.elementid).append("bottommargin']=30;");
                    html.append("\n</script>\n");
                } else if (this.sdiInfo.getDataSet(this.datasetName).getRowCount() == 0 && currentRow == -9999) {
                    html.append(SDITagUtil.getGrid((ArrayList)this.pageContext.getAttribute(this.elementid + "idGrid"), this.elementid, this.browser, this.getTranslationProcessor()));
                    html.append("\n<script>");
                    if (isOldScroll) {
                        html.append(this.elementid).append("handler1.setContainingdiv( '").append(this.elementid).append("br_div' )");
                    }
                    html.append("\nelementprefix['").append(this.elementid).append("']='").append(this.prefix).append("';");
                    html.append("\ncellsmap['").append(this.elementid).append("']=__").append(this.elementid).append("cells;");
                    html.append("\nhandlermap['").append(this.elementid).append("']=").append(this.elementid).append("handler1;");
                    html.append(this.getColumnidlistJs());
                    if (this.pageContext.getAttribute(this.elementid + "_columnidlist") != null && this.pageContext.getAttribute(this.elementid + "_columnidlist").toString().length() > 0) {
                        html.append("\ncolumnlistmap['").append(this.elementid).append("_columnidlist']='").append(this.pageContext.getAttribute(this.elementid + "_columnidlist").toString().substring(1)).append("';");
                    }
                    html.append("\nscrollgridspecmap['").append(this.elementid).append("rightmargin']=30;");
                    html.append("\nscrollgridspecmap['").append(this.elementid).append("bottommargin']=30;");
                    html.append("\n</script>\n");
                } else if (currentRow == -9999) {
                    // empty if block
                }
                if (this.pageContext.getAttribute("dd_dropdownvalues") != null) {
                    html.append("\n<script>");
                    html.append(this.pageContext.getAttribute("dd_dropdownvalues"));
                    html.append("\n</script>\n");
                }
            }
        }
        if (currentRow == this.sdiInfo.getRowCount(this.datasetName) - 1 || this.sdiInfo.getRowCount(this.datasetName) == 0 && currentRow == -9999) {
            html.append(this.getValidationScript(this.element.getCollection("columns"), this.pageContext));
        }
        return html.toString();
    }

    private String getCommonJsIncludes() {
        StringBuffer html = new StringBuffer();
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/lookup.js\"></script>\n");
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/maint.js\"></script>\n");
        if (!this.insidesdiform) {
            html.append("<script language=\"JavaScript\" src=\"WEB-CORE/scripts/tags.js\"></script>\n");
        }
        PropertyListCollection columns = this.element.getCollection("columns");
        for (int i = 0; i < columns.size(); ++i) {
            String mode = columns.getPropertyList(i).getProperty("mode");
            if (!"password".equals(mode)) continue;
            html.append(HttpUtil.getEncryptionJS());
            break;
        }
        String dddivid = "dd_div";
        int dd_f = 0;
        if (this.pageContext != null) {
            if (this.pageContext.getAttribute("_dd_div_id") != null && this.pageContext.getAttribute("_dd_div_id") instanceof Integer) {
                dd_f = (Integer)this.pageContext.getAttribute("_dd_div_id");
                ++dd_f;
            }
            this.pageContext.setAttribute("_dd_div_id", (Object)new Integer(dd_f));
        }
        if (dd_f > 0) {
            dddivid = dddivid + "_" + dd_f;
        }
        html.append("<div tabindex = \"0\" style=\"position:absolute; display:none\" _ddid=\"").append(dd_f).append("\" id=\"").append(dddivid).append("\" class=\"dropdowndiv\" onkeydown=\"dd_divKeyPress()\" onmouseover=\"this.onblur = null;\" onmouseout=\"this.onblur = dd_divBlur;\" onfocusout=\"dd_hideDropDown(").append(dddivid).append(");\"></div>\n");
        html.append("<textarea id=\"gridtextarea\" onblur=\"hideGridTextArea()\" onmouseout=\"this.blur();this.onblur()\" onchange=\"currentTextAreaChanged( this )\" onkeyup=\"currentTextAreaChanged( this )\" rows=\"10\" cols=\"40\" style=\"border:1px solid;display: none; z-index: 100; position: absolute; top: 100px; left: 100px\"></textarea>\n");
        return html.toString();
    }

    private String getColumnidlistJs() {
        PropertyList sdc = (PropertyList)this.pageContext.getAttribute(this.sdcid + "_props");
        if (sdc == null) {
            return "";
        }
        String keycolid1 = sdc.getProperty("keycolid1");
        String keycolid2 = sdc.getProperty("keycolid2");
        String keycolid3 = sdc.getProperty("keycolid3");
        return "\ncolumnlistmap['" + this.element.getId() + "_keycolidlist']='" + keycolid1 + (keycolid2.length() == 0 ? "" : ";" + keycolid2) + (keycolid3.length() == 0 ? "" : ";" + keycolid3) + "';";
    }

    private String getGridJsIncludes() {
        StringBuffer html = new StringBuffer();
        html.append(this.getCommonJsIncludes());
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/scripts/grid.js\"></script>\n");
        html.append("<textarea style=\"display:none;width:0;height:0\" id=\"clipboard\"></textarea>\n");
        return html.toString();
    }

    public String getValidationScript(PropertyListCollection columns, PageContext pageContext) {
        PropertyListCollection valifields = new PropertyListCollection();
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList attributes = columns.getPropertyList(i);
            if ((attributes.get("validation") == null || attributes.getProperty("validation").length() <= 0) && (attributes.get("customjs") == null || attributes.getProperty("customjs").length() <= 0)) continue;
            boolean isMaskedData = false;
            DataSet queryData = this.sdiInfo.getDataSet(this.datasetName);
            if (queryData != null && queryData.getRowCount() > 0) {
                isMaskedData = queryData.isMasked(0, attributes.getProperty("columnid"));
            }
            if (isMaskedData) continue;
            PropertyList valifield = new PropertyList();
            valifield.setProperty("name", attributes.getProperty("columnid"));
            valifield.setProperty("validation", attributes.getProperty("validation"));
            valifield.setProperty("customjs", attributes.getProperty("customjs"));
            valifields.add(valifield);
        }
        StringBuffer html = new StringBuffer();
        if (valifields.size() > 0) {
            html.append("\n<script>\n");
            StringBuffer customHtml = null;
            for (int i = 0; i < valifields.size(); ++i) {
                PropertyList field = valifields.getPropertyList(i);
                String name = field.getProperty("name");
                String valitypes = field.getProperty("validation");
                String custvalijs = field.getProperty("customjs");
                if (valitypes != null || valitypes.length() > 0) {
                    html.append("validations[ '").append(this.datasetName).append(";").append(name).append("'] = '").append(valitypes).append("';\n");
                }
                if (custvalijs == null || custvalijs.length() <= 0) continue;
                if (customHtml == null) {
                    customHtml = !"primary".equals(this.datasetName) ? new StringBuffer("function " + this.datasetName + "_validateCustom( fieldid, type ){\n") : new StringBuffer("function validateCustom( fieldid, type ){\n");
                }
                customHtml.append("    if ( fieldid.indexOf( '_").append(name).append("' ) > 0 && fieldid.length == ( fieldid.indexOf( '").append(name).append("' ) + ").append(name.length()).append(") ){\nvar res = ").append(custvalijs).append("( fieldid, type );\nif(typeof(res) !='undefined' && res != ''){ res = fieldid + '|' + res}\nreturn res;\n}\n");
            }
            if (customHtml != null) {
                customHtml.append("  return '';\n");
                customHtml.append("}\n");
                html.append(customHtml);
            }
            html.append(Maint.getValidationVariables(this.element, pageContext));
            if (!"primary".equals(this.datasetName)) {
                html.append("validationdatasets[ '").append(this.datasetName).append("'] = '").append(this.datasetName).append("';\n");
            }
            html.append("\n</script>\n");
        }
        return html.toString();
    }

    public static String getValidationVariables(PropertyList element, PageContext pageContext) {
        StringBuffer html = new StringBuffer();
        html.append("if (typeof(sapdateformat) == 'undefined'){\n");
        html.append("var sapdateformat = ").append(RegexConverter.getSapDateFormat(pageContext)).append(";\n");
        html.append("var sapdateformat4DigitYear = ").append(RegexConverter.getSapDateFormat4or2DigitYear(pageContext, "4")).append(";\n");
        html.append("var sapdateformat2DigitYear = ").append(RegexConverter.getSapDateFormat4or2DigitYear(pageContext, "2")).append(";\n");
        html.append("var decimalSeparator = sapphire.connection.decimalSeparator;\n");
        html.append("var groupingSeparator = sapphire.connection.groupingSeparator;\n");
        html.append("}\n");
        if (element != null && element.containsKey("texts")) {
            html.append("if (typeof(texts) == 'undefined' || texts.length==0){\n");
            String[] textids = new String[]{"validationfail", "notnumber", "notinteger", "notdecimal", "lengthnotinrange", "mandatorynotfilled", "valuenotinrange", "notdate"};
            for (int i = 0; i < textids.length; ++i) {
                String message = ElementUtil.getText(element, textids[i], "");
                if (message.trim().length() <= 0) continue;
                html.append("texts['").append(textids[i]).append("'] = '").append(StringUtil.replaceAll(message, "'", "\\'")).append("';\n");
            }
            html.append("}\n");
        }
        return html.toString();
    }

    public void setInsidesdiform(boolean value) {
        this.insidesdiform = value;
    }

    public static ArrayList getGroups(String formstyle, PropertyListCollection columns, String defaultGroupId) {
        return Maint.getGroups(MaintStyle.getMaintStyle(formstyle), columns, defaultGroupId);
    }

    public static ArrayList getGroups(MaintStyle formstyle, PropertyListCollection columns, String defaultGroupId) {
        ArrayList<String> tabGroups = new ArrayList<String>();
        ArrayList<PropertyList> hiddenColumnList = new ArrayList<PropertyList>();
        for (int i = 0; i < columns.size(); ++i) {
            boolean dynamichidden;
            PropertyList column = columns.getPropertyList(i);
            if (formstyle == MaintStyle.FORM) {
                if (!tabGroups.contains(defaultGroupId)) {
                    tabGroups.add(defaultGroupId);
                }
                column.setProperty("groupid", defaultGroupId);
                continue;
            }
            String groupid = column.getProperty("groupid");
            if (groupid.length() == 0) {
                if ("hidden".equals(column.getProperty("mode"))) {
                    hiddenColumnList.add(column);
                } else {
                    groupid = defaultGroupId;
                    column.setProperty("groupid", groupid);
                }
            }
            boolean bl = dynamichidden = column.getPropertyList("dynamicrendering") != null && "Y".equals(column.getPropertyList("dynamicrendering").getProperty("hiddenif"));
            if (dynamichidden && !hiddenColumnList.contains(column)) {
                hiddenColumnList.add(column);
            }
            if (groupid.length() <= 0 || "hidden".equals(column.getProperty("mode")) || "retrievedata".equals(column.getProperty("mode")) || dynamichidden || tabGroups.contains(groupid)) continue;
            tabGroups.add(groupid);
        }
        if (formstyle != MaintStyle.FORM && tabGroups.size() > 0 && hiddenColumnList.size() > 0) {
            for (PropertyList column : hiddenColumnList) {
                column.setProperty("groupid", (String)tabGroups.get(0));
            }
        }
        return tabGroups;
    }

    public static void mergeRequestOverrides(PropertyList pagedata, PropertyList maintElement, PropertyList toolbarElement, PropertyList layout, PropertyList requestOverrides, boolean isMaintContent, PageContext pageContext) {
        block54: {
            block55: {
                PropertyListCollection t_buttons_ovv;
                String temp;
                PropertyList t_layout;
                if (requestOverrides == null) break block54;
                String sdcid = requestOverrides.getProperty("sdcid", "");
                if (sdcid.length() > 0) {
                    if (toolbarElement != null) {
                        toolbarElement.setId("advancedtoolbar");
                        toolbarElement.setProperty("sdcid", sdcid);
                    }
                    if (maintElement != null) {
                        maintElement.setId("maint");
                        maintElement.setProperty("sdcid", sdcid);
                    }
                }
                if (isMaintContent) break block55;
                if (layout != null && (t_layout = requestOverrides.getPropertyList("layout")) != null) {
                    temp = t_layout.getProperty("applicationtitle", "");
                    if (temp.length() > 0) {
                        layout.setProperty("applicationtitle", temp);
                    }
                    if ((temp = t_layout.getProperty("objectname", "")).length() > 0) {
                        layout.setProperty("objectname", temp);
                    }
                    if ((temp = t_layout.getProperty("stylesheet", "")).length() > 0) {
                        layout.setProperty("stylesheet", temp);
                    }
                    if ((temp = t_layout.getProperty("hidetitle", "")).length() > 0) {
                        layout.setProperty("hidetitle", temp);
                    }
                }
                if ((temp = requestOverrides.getProperty("title", "")).length() > 0) {
                    pagedata.setProperty("title", temp);
                }
                if (toolbarElement == null || (t_buttons_ovv = requestOverrides.getCollection("buttons")) == null) break block54;
                PropertyListCollection t_buttons = toolbarElement.getCollection("buttons");
                if (t_buttons == null) {
                    t_buttons = new PropertyListCollection();
                    toolbarElement.setProperty("buttons", t_buttons);
                }
                for (int t_i = 0; t_i < t_buttons_ovv.size(); ++t_i) {
                    PropertyList t_btn_cp;
                    PropertyList t_btn_ovv = t_buttons_ovv.getPropertyList(t_i);
                    PropertyList t_btn = null;
                    String t_id = t_btn_ovv.getProperty("id", "");
                    PropertyList t_btn_cp_ovv = t_btn_ovv.getPropertyList("commonprops");
                    String t_text = "";
                    String t_tip = "";
                    String t_image = "";
                    String t_show = "";
                    if (t_btn_cp_ovv != null) {
                        t_text = t_btn_cp_ovv.getProperty("text", "");
                        t_tip = t_btn_cp_ovv.getProperty("tip", "");
                        t_image = t_btn_cp_ovv.getProperty("image", "");
                        t_show = t_btn_cp_ovv.getProperty("show", "");
                        if (t_id.length() > 0 && (t_btn = t_buttons.getPropertyList(t_id)) == null) {
                            t_btn = t_buttons.find("id", t_id);
                        }
                        if (t_btn == null && t_text.length() > 0) {
                            for (PropertyList pl : t_buttons) {
                                PropertyList plcp;
                                if (pl == null || (plcp = pl.getPropertyList("commonprops")) == null || !plcp.getProperty("text").equals(t_text)) continue;
                                t_btn = pl;
                                break;
                            }
                        }
                    }
                    if (t_btn == null) {
                        t_btn = new PropertyList();
                        t_btn.setId("override" + t_i);
                        t_buttons.add(t_btn);
                    }
                    if ((t_btn_cp = t_btn.getPropertyList("commonprops")) == null) {
                        t_btn_cp = new PropertyList();
                        t_btn_cp.setId("commonprops");
                        t_btn.setProperty("commonprops", t_btn_cp);
                        t_btn_cp.setProperty("id", "override" + t_i);
                    }
                    if (t_text.length() > 0) {
                        t_btn_cp.setProperty("text", t_text);
                    }
                    if (t_tip.length() > 0) {
                        t_btn_cp.setProperty("tip", t_tip);
                    }
                    if (t_image.length() > 0) {
                        t_btn_cp.setProperty("image", t_image);
                    }
                    if (t_show.length() > 0) {
                        t_btn_cp.setProperty("show", t_show);
                    } else {
                        t_btn_cp.setProperty("show", "Y");
                    }
                    PropertyList t_btn_sp_ovv = t_btn_ovv.getPropertyList("standardbuttonprops");
                    if (t_btn_sp_ovv == null) continue;
                    t_btn.setProperty("buttontype", "Standard");
                    PropertyList t_btn_sp = t_btn.getPropertyList("standardbuttonprops");
                    if (t_btn_sp == null) {
                        t_btn_sp = new PropertyList();
                        t_btn_sp.setId("standardbuttonprops");
                        t_btn.setProperty("standardbuttonprops", t_btn_sp);
                    }
                    if ((temp = t_btn_sp_ovv.getProperty("action", "")).length() > 0) {
                        t_btn_sp.setProperty("action", temp);
                    }
                    if ((temp = t_btn_sp_ovv.getProperty("page", "")).length() > 0) {
                        t_btn_sp.setProperty("page", temp);
                    }
                    if ((temp = t_btn_sp_ovv.getProperty("target", "")).length() <= 0) continue;
                    t_btn_sp.setProperty("target", temp);
                }
                break block54;
            }
            if (maintElement != null) {
                PropertyListCollection t_columns_ovv;
                PropertyListCollection t_columns = maintElement.getCollection("columns");
                if (t_columns == null) {
                    t_columns = new PropertyListCollection();
                    maintElement.setProperty("columns", t_columns);
                }
                if ((t_columns_ovv = requestOverrides.getCollection("columns")) != null) {
                    PropertyList sdc = null;
                    for (int t_i = 0; t_i < t_columns_ovv.size(); ++t_i) {
                        PropertyList t_col_ovv = t_columns_ovv.getPropertyList(t_i);
                        PropertyList t_col = null;
                        String t_id = t_col_ovv.getProperty("id", "");
                        if (t_id.length() > 0 && (t_col = t_columns.getPropertyList(t_id)) == null) {
                            t_col = t_columns.find("id", t_id);
                        }
                        String t_colid = t_col_ovv.getProperty("columnid", "");
                        if (t_col == null && t_colid.length() > 0 && (t_col = t_columns.find("columnid", t_colid)) == null && (sdc != null || maintElement != null && pageContext != null && maintElement.getProperty("sdcid", "").length() > 0)) {
                            if (sdc == null) {
                                sdc = new SDCProcessor(pageContext).getPropertyList(maintElement.getProperty("sdcid", ""));
                            }
                            if (sdc != null) {
                                String keycolid1 = sdc.getProperty("keycolid1", "");
                                String keycolid2 = sdc.getProperty("keycolid2", "");
                                String keycolid3 = sdc.getProperty("keycolid3", "");
                                String desccol = sdc.getProperty("desccol", "");
                                if (t_colid.equalsIgnoreCase("keycolid1") && keycolid1.length() > 0) {
                                    t_col = t_columns.find("columnid", keycolid1);
                                } else if (t_colid.equalsIgnoreCase("keycolid2") && keycolid2.length() > 0) {
                                    t_col = t_columns.find("columnid", keycolid2);
                                } else if (t_colid.equalsIgnoreCase("keycolid3") && keycolid3.length() > 0) {
                                    t_col = t_columns.find("columnid", keycolid3);
                                } else if (t_colid.equalsIgnoreCase("desccol") && desccol.length() > 0) {
                                    t_col = t_columns.find("columnid", desccol);
                                } else if (t_colid.equalsIgnoreCase(keycolid1)) {
                                    t_col = t_columns.find("columnid", "keycolid1");
                                } else if (t_colid.equalsIgnoreCase(keycolid2)) {
                                    t_col = t_columns.find("columnid", "keycolid2");
                                } else if (t_colid.equalsIgnoreCase(keycolid3)) {
                                    t_col = t_columns.find("columnid", "keycolid3");
                                } else if (t_colid.equalsIgnoreCase(desccol)) {
                                    t_col = t_columns.find("columnid", "desccol");
                                }
                            }
                        }
                        if (t_col != null) {
                            String temp;
                            if (t_id.length() > 0) {
                                t_col.setProperty("id", t_id);
                            }
                            if (t_colid.length() > 0) {
                                t_col.setProperty("columnid", t_colid);
                            }
                            if ((temp = t_col_ovv.getProperty("title", "")).length() > 0) {
                                t_col.setProperty("title", temp);
                            }
                            if ((temp = t_col_ovv.getProperty("mode", "")).length() > 0) {
                                t_col.setProperty("mode", temp);
                            }
                            if ((temp = t_col_ovv.getProperty("displayvalue", "")).length() > 0) {
                                t_col.setProperty("displayvalue", temp);
                            }
                            if ((temp = t_col_ovv.getProperty("pseudocolumn", "")).length() <= 0) continue;
                            t_col.setProperty("pseudocolumn", temp);
                            continue;
                        }
                        t_col = (PropertyList)t_col_ovv.clone();
                        t_columns.add(t_col);
                    }
                }
            }
        }
    }

    public static boolean isNewAddMode(PageContext pageContext, PropertyList pagedata, PropertyList maint) {
        boolean newAddMode;
        RequestContext rc = RequestContext.getRequestContext(pageContext);
        boolean showDetail = "Yes".equalsIgnoreCase(pagedata.getProperty("showdetail", "No"));
        Pattern pattern = Pattern.compile("\\(auto_keyid1_(\\d)+\\)");
        String keyid1 = pagedata.getProperty("keyid1");
        String template = rc.getProperty("template");
        PropertyList templateselector = rc.getPropertyList("templateselector");
        boolean isTemplateMode = template != null && template.length() > 0 || templateselector != null && "Y".equalsIgnoreCase(templateselector.getPropertyList("templateretrieval").getProperty("show"));
        boolean bl = newAddMode = ("".equals(keyid1) || pattern.matcher(keyid1).matches()) && "Add".equalsIgnoreCase(pagedata.getProperty("mode")) && showDetail && !isTemplateMode;
        if (newAddMode) {
            String sdcid1 = pagedata.getProperty("sdcid");
            if (sdcid1 == null || sdcid1.trim().length() == 0) {
                sdcid1 = maint.getProperty("sdcid");
            }
            int keyColCount = Integer.parseInt(new SDCProcessor(pageContext).getProperty(sdcid1, "keycolumns"));
            for (int i = 1; i <= keyColCount; ++i) {
                String dummyKey = "keyid" + i;
                String dummyValue = "(auto_keyid" + i + "_0)";
                pagedata.setProperty(dummyKey, dummyValue);
                rc.setProperty(dummyKey, dummyValue);
            }
            pagedata.setProperty("newAddMode", "true");
            rc.setProperty("pagedata", pagedata);
            pageContext.setAttribute("pagedata", (Object)pagedata);
        }
        return newAddMode;
    }

    public static boolean isDetailVisibleOnAdd(PropertyList detailElement, PageContext pageContext) throws JspTagException {
        RequestContext rc = RequestContext.getRequestContext(pageContext);
        String elementid = detailElement.getProperty("elementid");
        String elementType = detailElement.getProperty("elementtype");
        String elementClassname = "";
        PropertyList detail = (PropertyList)JstlUtil.evaluateExpression("${" + elementid + "}", pageContext);
        QueryProcessor qp = new QueryProcessor(pageContext);
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = qp.getPreparedSqlDataSet("SELECT objectname FROM propertytree WHERE propertytreeid = " + safeSQL.addVar(elementType), safeSQL.getValues());
        String string = elementClassname = ds != null && ds.size() == 1 ? ds.getString(0, "objectname") : "";
        if (elementClassname == null || elementClassname.length() == 0) {
            throw new JspTagException("ElementTag exception: class not defined or not found using type '" + elementType + "'");
        }
        try {
            BaseElement element = (BaseElement)Class.forName(elementClassname).newInstance();
            element.setElementid(elementid);
            element.setElementType(elementType);
            element.setElementClass(elementClassname);
            element.setPageContext(pageContext);
            element.setConnectionId(rc.getConnectionId());
            element.setElementProperties(detail);
            return element.isVisibleInAddMode();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String resolvedKeyId(String keyid, int index) {
        Pattern pattern = Pattern.compile("\\(auto_keyid" + index + "_(\\d)*\\)");
        return pattern.matcher(keyid).matches() ? "" : keyid;
    }

    public static enum MaintStyle {
        FORM("Form", false, false, false, false),
        FORMWITHTABGROUPS("FormWithTabGroups", false, false, true, false),
        FORMWITHFIELDGROUPS("FormWithFieldGroups", false, true, false, false),
        GRID("Grid", true, false, false, false),
        GRIDWITHCHECKBOX("GridWithCheckBox", true, true, false, true),
        GRIDWITHTABGROUPS("GridWithTabGroups", true, false, true, true);

        private String name;
        private boolean grid;
        private boolean fieldgroups;
        private boolean tabgroups;
        private boolean checkbox;

        private MaintStyle(String name, boolean isGrid, boolean hasFieldGroups, boolean hasTabGroups, boolean hasCheckbox) {
            this.name = name;
            this.grid = isGrid;
            this.fieldgroups = hasFieldGroups;
            this.tabgroups = hasTabGroups;
            this.checkbox = hasCheckbox;
        }

        public String getName() {
            return this.name;
        }

        public boolean isGrid() {
            return this.grid;
        }

        public boolean hasFieldGroups() {
            return this.fieldgroups;
        }

        public boolean hasTabGroups() {
            return this.tabgroups;
        }

        public boolean hasCheckbox() {
            return this.checkbox;
        }

        public static MaintStyle getMaintStyle(String elementStyle, String passedInStyle, int rowcount, String copies) {
            MaintStyle maintstyle = MaintStyle.getMaintStyle(elementStyle);
            if (rowcount > 1 || copies != null && !copies.equalsIgnoreCase("") && Integer.valueOf(copies) > 1) {
                MaintStyle passedinmainttype;
                MaintStyle maintStyle = passedinmainttype = passedInStyle.length() > 0 ? MaintStyle.getMaintStyle(passedInStyle) : maintstyle;
                if (passedinmainttype.isGrid() && !maintstyle.isGrid()) {
                    maintstyle = maintstyle.hasTabGroups() && !passedinmainttype.hasTabGroups() ? GRIDWITHTABGROUPS : passedinmainttype;
                }
            }
            return maintstyle;
        }

        public static MaintStyle getMaintStyle(String style) {
            if (style != null && style.length() > 0) {
                try {
                    return MaintStyle.valueOf(style.toUpperCase());
                }
                catch (Exception e) {
                    return FORM;
                }
            }
            return FORM;
        }
    }
}

