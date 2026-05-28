/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.list;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.SdcInfo;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.list.CalendarHelper;
import com.labvantage.sapphire.pageelements.list.ListAjaxRequest;
import com.labvantage.sapphire.pageelements.list.ListTable;
import com.labvantage.sapphire.pageelements.list.MapHelper;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.SDITagUtil;
import com.labvantage.sapphire.util.dhtmlxscheduler.DHTMLXScheduler;
import com.labvantage.sapphire.util.groovy.GroovyPolicyUtil;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class List
extends BaseElement {
    SDCProcessor sdcProcessor;
    String tagsortby;
    String keycolid1 = "";
    String keycolid2 = "";
    String keycolid3 = "";
    String desccol = "";
    String sdcid = "";
    String overridepageorder = "";
    String listmode = "";
    MapHelper mapHelper = null;
    CalendarHelper calendarHelper = null;
    public static final String PROPERTY_LIST_GROUPBY = "_listbygroup";
    private String datasetName = "primary";
    private PropertyList sdc;

    public List() {
    }

    public List(PageContext pageContext, SDITagInfo sdiInfo, SDCProcessor sdcProcessor) {
        this.pageContext = pageContext;
        this.sdiInfo = sdiInfo;
        this.sdcProcessor = sdcProcessor;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public void setListmode(String listmode) {
        this.listmode = listmode;
    }

    public void setSortby(String sortby) {
        this.tagsortby = sortby;
    }

    public void setOverridepageorder(String overridepageorder) {
        this.overridepageorder = overridepageorder;
    }

    private StringBuffer getPagingHTMLStart(String sortbyColumn, String groupbyColumn, String filterbyColumn, String filterbyValue, boolean isPagingRequest, String command, int currentpage, String pageorfile, int totalrows, int rowsperpage, int qualifiedrows, QueryData queryData) {
        StringBuffer paginghtml = new StringBuffer();
        boolean hasKeyid2 = false;
        boolean hasKeyid3 = false;
        if (this.keycolid2.length() > 0) {
            hasKeyid2 = true;
        }
        if (this.keycolid3.length() > 0) {
            hasKeyid3 = true;
        }
        paginghtml.append("\n<form id=\"pagingform\" name=\"pagingform\" method=\"post\" action=\"rc?command=").append(command).append("&").append(command).append("=").append(pageorfile);
        if (this.pageContext.getRequest().getParameter("_iframename") != null) {
            paginghtml.append("&_iframename=").append(this.pageContext.getRequest().getParameter("_iframename"));
        }
        paginghtml.append("\" >\n");
        paginghtml.append("<input name=\"pageno\" type=\"hidden\" value=\"0\"/>\n");
        paginghtml.append("<input name=\"listmode\" type=\"hidden\" value=\"list\"/>\n");
        paginghtml.append("<input name=\"rowsperpage\" type=\"hidden\" value=\"").append(rowsperpage).append("\"/>\n");
        paginghtml.append("<input name=\"sortby\" type=\"hidden\" value=\"").append(sortbyColumn).append("\"/>\n");
        paginghtml.append("<input name=\"groupby\" type=\"hidden\" value=\"").append(groupbyColumn).append("\"/>\n");
        paginghtml.append("<input name=\"selectionmode\" type=\"hidden\" value=\"").append("").append("\"/>\n");
        paginghtml.append("<input name=\"currentpage\" id=\"currentpage\" type=\"hidden\" value=\"").append(currentpage).append("\"/>\n");
        String[] scancolumnids = StringUtil.split(this.element.getProperty("scancolumnid"), ";");
        if (!isPagingRequest || isPagingRequest && currentpage == 0) {
            StringBuffer keyid1list = new StringBuffer();
            StringBuffer keyid2list = new StringBuffer();
            StringBuffer keyid3list = new StringBuffer();
            StringBuffer[] scanidLists = new StringBuffer[scancolumnids.length];
            paginghtml.append("<input name=\"totalrows\" type=\"hidden\" value=\"").append(totalrows).append("\"/>\n");
            paginghtml.append("<input name=\"qualifiedrows\" type=\"hidden\" value=\"").append(qualifiedrows).append("\"/>\n");
            for (int i = 0; i < totalrows; ++i) {
                int s;
                if (i == 0) {
                    keyid1list.append(queryData.getValue(i, this.keycolid1, "no value"));
                    if (hasKeyid2) {
                        keyid2list.append(queryData.getValue(i, this.keycolid2, "no value"));
                    }
                    if (hasKeyid3) {
                        keyid3list.append(queryData.getValue(i, this.keycolid3, "no value"));
                    }
                    for (s = 0; s < scancolumnids.length; ++s) {
                        scanidLists[s] = new StringBuffer();
                        if (scancolumnids[s].length() <= 0) continue;
                        scanidLists[s].append(queryData.getValue(i, scancolumnids[s], ""));
                    }
                    continue;
                }
                keyid1list.append(";").append(queryData.getValue(i, this.keycolid1, "no value"));
                if (hasKeyid2) {
                    keyid2list.append(";").append(queryData.getValue(i, this.keycolid2, "no value"));
                }
                if (hasKeyid3) {
                    keyid3list.append(";").append(queryData.getValue(i, this.keycolid3, "no value"));
                }
                for (s = 0; s < scancolumnids.length; ++s) {
                    if (scancolumnids[s].length() <= 0) continue;
                    scanidLists[s].append(";").append(queryData.getValue(i, scancolumnids[s], ""));
                }
            }
            paginghtml.append("<input name=\"sdcid\" type=\"hidden\" value=\"").append(this.sdcid).append("\"/>\n");
            paginghtml.append("<input name=\"keyid1\" type=\"hidden\" value=\"").append(keyid1list.toString()).append("\"/>\n");
            if (hasKeyid2) {
                paginghtml.append("<input name=\"keyid2\" type=\"hidden\" value=\"").append(keyid2list.toString()).append("\">\n");
            }
            if (hasKeyid3) {
                paginghtml.append("<input name=\"keyid3\" type=\"hidden\" value=\"").append(keyid3list.toString()).append("\">\n");
            }
            for (int s = 0; s < scancolumnids.length; ++s) {
                if (scancolumnids[s].length() <= 0) continue;
                paginghtml.append("<input name=\"scanid_" + s + "\" id=\"scanid_" + s + "\" type=\"hidden\" value=\"").append(scanidLists[s] != null ? scanidLists[s].toString() : "").append("\">\n");
            }
            if (rowsperpage > 0 && totalrows > rowsperpage) {
                for (int j = totalrows - 1; j >= rowsperpage; --j) {
                    queryData.getQuerydata().deleteRow(j);
                }
            }
        } else {
            paginghtml.append("<input name=\"totalrows\" type=\"hidden\" value=\"").append(totalrows).append("\">\n");
            paginghtml.append("<input name=\"qualifiedrows\" type=\"hidden\" value=\"").append(qualifiedrows).append("\">\n");
            paginghtml.append("<input name=\"sdcid\" type=\"hidden\" value=\"").append(this.sdcid).append("\">\n");
            paginghtml.append("<input name=\"keyid1\" type=\"hidden\" value=\"").append(this.pageContext.getRequest().getParameter("keyid1")).append("\">\n");
            if (hasKeyid2) {
                paginghtml.append("<input name=\"keyid2\" type=\"hidden\" value=\"").append(this.pageContext.getRequest().getParameter("keyid2")).append("\">\n");
            }
            if (hasKeyid3) {
                paginghtml.append("<input name=\"keyid3\" type=\"hidden\" value=\"").append(this.pageContext.getRequest().getParameter("keyid3")).append("\">\n");
            }
            for (int s = 0; s < scancolumnids.length; ++s) {
                if (scancolumnids[s].length() <= 0) continue;
                paginghtml.append("<input name=\"scanid_" + s + "\" id=\"scanid_" + s + "\" type=\"hidden\" value=\"").append(this.pageContext.getRequest().getParameter("scanid_" + s)).append("\">\n");
            }
        }
        if (this.requestContext == null) {
            this.requestContext = RequestContext.getRequestContext(this.pageContext);
        }
        paginghtml.append("<textarea style=\"display:none;\" name=\"__pagedirectives\" type=\"hidden\">").append(this.requestContext.getPropertyList("pagedirectives") != null ? this.requestContext.getPropertyList("pagedirectives").toJSONString() : "").append("</textarea>\n");
        return paginghtml;
    }

    private StringBuffer getPagingHTMLEnd(String returncolumns, String rowcolumns, DataSet originalData, PropertyList pagedata) {
        String selectedvalues;
        StringBuffer paginghtml = new StringBuffer();
        String selectedkeyids = this.pageContext.getRequest().getParameter("selectedkeyids") == null ? "" : this.pageContext.getRequest().getParameter("selectedkeyids");
        String[] array_selectedkeyids = null;
        if (selectedkeyids.length() == 0) {
            selectedkeyids = pagedata == null ? (this.pageContext.getRequest().getParameter("restoreselected") == null ? "" : this.pageContext.getRequest().getParameter("restoreselected")) : pagedata.getProperty("restoreselected", "");
            array_selectedkeyids = StringUtil.split(selectedkeyids, "%3B");
        }
        String allselected = this.pageContext.getRequest().getParameter("allselected") == null ? "" : this.pageContext.getRequest().getParameter("allselected");
        String string = selectedvalues = this.pageContext.getRequest().getParameter("selectedvalues") == null ? "" : this.pageContext.getRequest().getParameter("selectedvalues");
        if (selectedkeyids.length() > 0 && (allselected.length() == 0 || selectedvalues.length() == 0)) {
            String[] array_return_cols = null;
            if (returncolumns.length() > 0) {
                array_return_cols = StringUtil.split(returncolumns, ";");
            }
            String[] array_row_cols = null;
            if (rowcolumns.length() > 0) {
                array_row_cols = StringUtil.split(rowcolumns, ";");
            }
            if (array_selectedkeyids == null) {
                array_selectedkeyids = selectedkeyids.indexOf(";") > 0 && selectedkeyids.indexOf("%3B") < 0 ? StringUtil.split(selectedkeyids, ";") : StringUtil.split(selectedkeyids, "%3B");
            }
            StringBuffer buf_selectedkeyids = new StringBuffer();
            StringBuffer buf_selectedvalues = new StringBuffer();
            StringBuffer buf_selectedreturn = new StringBuffer();
            boolean added = false;
            for (int row = 0; row < array_selectedkeyids.length; ++row) {
                int col;
                int foundRow;
                String[] array_row_keys;
                String r = array_selectedkeyids[row].trim();
                String[] stringArray = array_row_keys = r.length() > 0 ? StringUtil.split(r, ";") : new String[]{};
                if (this.keycolid1.length() <= 0 || array_row_keys.length <= 0) continue;
                StringBuffer rowkey = new StringBuffer(array_row_keys[0]);
                HashMap<String, String> findMap = new HashMap<String, String>();
                findMap.put(this.keycolid1, array_row_keys[0]);
                if (this.keycolid2.length() > 0 && array_row_keys.length > 1) {
                    rowkey.append(";").append(array_row_keys[1]);
                    findMap.put(this.keycolid2, array_row_keys[1]);
                    if (this.keycolid3.length() > 0 && array_row_keys.length > 2) {
                        rowkey.append(";").append(array_row_keys[2]);
                        findMap.put(this.keycolid3, array_row_keys[2]);
                    }
                }
                if ((foundRow = originalData.findRow(findMap)) <= -1) continue;
                if (added) {
                    buf_selectedkeyids.append("%3B");
                    buf_selectedvalues.append("%3B");
                    buf_selectedreturn.append("%3B");
                }
                buf_selectedkeyids.append(rowkey);
                if (array_return_cols != null && array_return_cols.length > 0) {
                    StringBuffer buff_rcol = new StringBuffer();
                    for (col = 0; col < array_return_cols.length; ++col) {
                        if (col > 0) {
                            buff_rcol.append("|");
                        }
                        buff_rcol.append(originalData.getValue(foundRow, array_return_cols[col], ""));
                    }
                    if (buff_rcol.length() > 0) {
                        buf_selectedreturn.append(buff_rcol);
                    }
                }
                if (array_row_cols != null && array_row_cols.length > 0) {
                    StringBuffer buff_vcol = new StringBuffer();
                    for (col = 0; col < array_row_cols.length; ++col) {
                        if (col > 0) {
                            buff_vcol.append("|");
                        }
                        buff_vcol.append(originalData.getValue(foundRow, array_row_cols[col], ""));
                    }
                    if (buff_vcol.length() > 0) {
                        buf_selectedvalues.append(buff_vcol);
                    }
                }
                added = true;
            }
            if (buf_selectedkeyids.length() > 0) {
                if (this.element.getProperty("initselectall").equalsIgnoreCase("Y") && selectedkeyids.length() > 0 && allselected.length() == 0 && selectedvalues.length() == 0) {
                    allselected = "Y";
                    selectedkeyids = "*";
                } else {
                    allselected = buf_selectedreturn.toString();
                    selectedkeyids = buf_selectedkeyids.toString();
                }
                selectedvalues = buf_selectedvalues.toString();
            } else if (this.element.getProperty("initselectall").equalsIgnoreCase("Y") && selectedkeyids.length() > 0 && allselected.length() == 0 && selectedvalues.length() == 0) {
                allselected = "Y";
                selectedkeyids = "*";
                selectedvalues = "";
            } else {
                allselected = "";
                selectedkeyids = "";
                selectedvalues = "";
            }
        } else if (this.element.getProperty("initselectall").equalsIgnoreCase("Y")) {
            allselected = "Y";
            selectedkeyids = "*";
        }
        boolean isRadio = "radiobutton".equals(this.element.getProperty("selectortype"));
        paginghtml.append("<input name=\"allselected\" type=\"hidden\" value=\"").append(isRadio ? "" : SafeHTML.encodeForHTMLAttribute(allselected)).append("\"/>\n");
        paginghtml.append("<input id=\"selectedkeyids\" name=\"selectedkeyids\" type=\"hidden\" value=\"").append(isRadio ? "" : SafeHTML.encodeForHTMLAttribute(selectedkeyids)).append("\"/>\n");
        paginghtml.append("<input id=\"selectedvalues\" name=\"selectedvalues\" type=\"hidden\" value=\"").append(isRadio ? "" : SafeHTML.encodeForHTMLAttribute(selectedvalues)).append("\"/>\n");
        paginghtml.append("<input type=\"hidden\" name=\"__formid\" value=\"pagingform\">\n");
        paginghtml.append("</form>");
        return paginghtml;
    }

    private static boolean buildQueryWhere(StringBuilder querywhere, PropertyList pagedata) {
        boolean mergequerywhere = false;
        String req_reswhere = EncryptDecrypt.unobfsql(pagedata.getProperty("extrarestrictivewhere"));
        String restrictiveWhere = EncryptDecrypt.unobfsql(pagedata.getProperty("restrictivewhere"));
        String mw = pagedata.getProperty("mergewhere", "Merge");
        if (restrictiveWhere.length() > 0 && req_reswhere.length() > 0) {
            if (!mw.equalsIgnoreCase("ignore")) {
                if (mw.equalsIgnoreCase("override")) {
                    restrictiveWhere = req_reswhere;
                } else if (mw.equalsIgnoreCase("merge")) {
                    restrictiveWhere = "( " + restrictiveWhere + " ) AND ( " + req_reswhere + " )";
                }
            }
        } else if (restrictiveWhere.length() == 0 && req_reswhere.length() > 0 && !mw.equalsIgnoreCase("ignore")) {
            restrictiveWhere = req_reswhere;
        }
        if (restrictiveWhere.length() > 0) {
            mergequerywhere = true;
            if (querywhere.length() > 0) {
                querywhere.insert(0, "(");
                querywhere.append(") AND ");
            }
            querywhere.append("(").append(restrictiveWhere).append(")");
        } else if (querywhere.length() > 0) {
            querywhere.insert(0, "( ");
            querywhere.append(" ) ");
        }
        return mergequerywhere;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean buildQueryWhere(SDIRequest sdiRequest, PropertyList pagedata, PropertyList listElement, PageContext pageContext, String attributeid) {
        StringBuilder querywhere = new StringBuilder(sdiRequest.getQueryWhere() != null ? sdiRequest.getQueryWhere() : "");
        boolean mergequerywhere = List.buildQueryWhere(querywhere, pagedata);
        if (attributeid == null || attributeid.length() == 0) {
            if (pagedata.getProperty("command").equals("page")) {
                pagedata = (PropertyList)pageContext.getRequest().getAttribute("pagedata");
                attributeid = pagedata.getProperty("page");
            } else {
                attributeid = pageContext.getRequest().getParameter("file");
            }
        }
        if (!pagedata.containsKey("__formid") || !pagedata.getProperty("__formid").equals("pagingform")) {
            PropertyListCollection groupby = listElement.getCollectionNotNull("groupby");
            ArrayList<String> filterList = new ArrayList<String>();
            StringBuilder cols = new StringBuilder();
            for (int index = 0; index < groupby.size(); ++index) {
                PropertyList plGroupBy = groupby.getPropertyList(index);
                boolean useasfilter = plGroupBy.getProperty("useasfilter", "N").equalsIgnoreCase("Y");
                if (!useasfilter) continue;
                String aliasCurr = plGroupBy.getProperty("columnid");
                filterList.add(aliasCurr);
                if (cols.length() > 0) {
                    cols.append(",");
                }
                cols.append(aliasCurr);
            }
            if (filterList.size() > 0) {
                String filterby;
                String filterbyOrg;
                SDIProcessor sdiProcessor = new SDIProcessor(pageContext);
                sdiRequest.setRequestItem("primary[" + cols.toString() + "]");
                sdiRequest.setQueryWhere(querywhere.toString());
                SDIData sdiData = null;
                String _oldFrom = "";
                try {
                    if (sdiRequest.getQueryid().length() > 0) {
                        _oldFrom = sdiRequest.getQueryFrom();
                        sdiRequest.setQueryFrom("");
                    }
                    sdiData = sdiProcessor.getSDIData(sdiRequest);
                }
                finally {
                    if (_oldFrom.length() > 0) {
                        sdiRequest.setQueryFrom(_oldFrom);
                    }
                }
                String defaultfilterval = "";
                String string = filterbyOrg = pagedata.containsKey("filterby") && pagedata.getProperty("filterby").length() > 0 ? pagedata.getProperty("filterby") : "";
                if (filterbyOrg.length() == 0 && attributeid != null && attributeid.length() > 0) {
                    filterbyOrg = (String)pageContext.getSession().getAttribute(attributeid + "_listfilterby");
                }
                String string2 = (filterby = filterbyOrg) != null ? (filterby.contains(" ") ? RequestParser.parseAlias(filterby) : filterby) : (filterby = filterby);
                if (sdiData != null && sdiData.getDataset("primary") != null) {
                    DataSet primary = sdiData.getDataset("primary");
                    if (filterby != null && filterby.length() > 0 && !filterby.equals("None")) {
                        defaultfilterval = primary.getRowCount() > 0 ? primary.getValue(0, filterby, "") : "";
                    }
                    for (int i = 0; i < filterList.size(); ++i) {
                        String al = RequestParser.parseAlias((String)filterList.get(i));
                        String fv = ((String)filterList.get(i)).contains(" ") ? al : (String)filterList.get(i);
                        String vals = primary.getColumnValues(fv, 0, primary.getRowCount(), ";", true);
                        pageContext.getSession().setAttribute(attributeid + "_listfilterdd_" + fv, (Object)vals);
                    }
                }
                if (filterby != null && filterby.length() > 0 && !filterby.equalsIgnoreCase("NONE")) {
                    String ffin;
                    String filtervalue;
                    mergequerywhere = true;
                    String string3 = filtervalue = pagedata.containsKey("filterbyvalue") ? pagedata.getProperty("filterbyvalue") : "";
                    if (filtervalue.length() == 0 && attributeid != null && attributeid.length() > 0) {
                        filtervalue = (String)pageContext.getSession().getAttribute(attributeid + "_listfilterbyvalue");
                    }
                    if (filtervalue == null || filtervalue.length() == 0 || filtervalue.equalsIgnoreCase("(null)")) {
                        if (defaultfilterval.length() == 0) {
                            filtervalue = "(null)";
                        } else {
                            filtervalue = defaultfilterval;
                            pageContext.getSession().setAttribute(attributeid + "_listfilterbyvalue", (Object)defaultfilterval);
                            pagedata.setProperty("filterbyvalue", defaultfilterval);
                        }
                    }
                    String string4 = filterbyOrg != null ? (filterbyOrg.contains(" ") ? RequestParser.parseColumn(filterbyOrg) : filterbyOrg) : (ffin = filterbyOrg);
                    if (ffin != null && ffin.length() > 0) {
                        if (querywhere.length() > 0) {
                            querywhere.append(" AND ");
                        }
                        if (filtervalue.length() > 0 && !filtervalue.equalsIgnoreCase("(null)") && !filtervalue.equalsIgnoreCase("None")) {
                            querywhere.append("(").append(ffin).append(" = '").append(filtervalue).append("'").append(")");
                        } else {
                            querywhere.append("(").append(ffin).append(" IS NULL ").append(")");
                        }
                    }
                }
            }
        }
        if (querywhere.length() > 0) {
            querywhere.insert(0, "( ");
            querywhere.append(" ) ");
        }
        sdiRequest.setQueryWhere(querywhere.toString());
        if (!(sdiRequest.getQueryFrom() != null && sdiRequest.getQueryFrom().length() != 0 || sdiRequest.getQueryid() != null && sdiRequest.getQueryid().length() > 0)) {
            String tableid = SdcInfo.getSDCProps(sdiRequest.getSDCid(), pageContext, "tableid");
            String sdcType = SdcInfo.getSDCProps(sdiRequest.getSDCid(), pageContext, "sdctype");
            if (sdcType != null && !"D".equalsIgnoreCase(sdcType)) {
                if (sdiRequest.getShowTemplatesOnly()) {
                    if (sdiRequest.getQueryWhere() == null || sdiRequest.getQueryWhere().length() == 0) {
                        sdiRequest.setQueryWhere(" ( " + tableid + ".templateflag = 'Y' ) ");
                    } else {
                        sdiRequest.setQueryWhere("(" + sdiRequest.getQueryWhere() + ") AND ( " + tableid + ".templateflag = 'Y' ) ");
                    }
                } else if (!sdiRequest.getShowTemplates()) {
                    if (sdiRequest.getQueryWhere() == null || sdiRequest.getQueryWhere().length() == 0) {
                        sdiRequest.setQueryWhere(" ( " + tableid + ".templateflag = 'N' OR " + tableid + ".templateflag IS NULL ) ");
                    } else {
                        sdiRequest.setQueryWhere("(" + sdiRequest.getQueryWhere() + ") AND ( " + tableid + ".templateflag = 'N' OR " + tableid + ".templateflag IS NULL ) ");
                    }
                }
            }
        }
        return mergequerywhere;
    }

    private String getFilterHTML(TranslationProcessor tp, String requestfilterbyColumn, String requestfilterbyValue, PropertyListCollection groupby, String filterbyColumn, PropertyList currentfilterby, String attributeid) {
        StringBuilder html = new StringBuilder();
        html.append(ElementUtil.getText(this.element, "filterby", "Filter By", tp)).append("&nbsp;<select id=\"filterbycolumns\" name=\"filterbycolumns\" " + (this.browser != null && this.browser.isPhone() ? "style=\"font-size:16px\"" : "") + " onchange=\"if( typeof(changeFilter) !='undefined')changeFilter(this);\">\n");
        html.append("<option value=\"NONE\" ").append("NONE".equals(requestfilterbyColumn) ? "selected>" : ">").append(tp.translate("None"));
        for (int index = 0; index < groupby.size(); ++index) {
            PropertyList plGroupBy = groupby.getPropertyList(index);
            boolean useasfilter = plGroupBy.getProperty("useasfilter", "N").equalsIgnoreCase("Y");
            if (!useasfilter) continue;
            String aliasCurr = plGroupBy.getProperty("columnid").contains(" ") ? RequestParser.parseAlias(plGroupBy.getProperty("columnid")) : plGroupBy.getProperty("columnid");
            String aliasReq = filterbyColumn.contains(" ") ? RequestParser.parseAlias(filterbyColumn) : filterbyColumn;
            html.append("<option value=\"").append(plGroupBy.getProperty("columnid")).append("\"").append(aliasCurr.equals(aliasReq) ? "selected>" : ">").append(plGroupBy.getProperty("title")).append("</option>");
        }
        html.append("</select>");
        if (!"NONE".equals(requestfilterbyColumn)) {
            html.append("&nbsp;");
            html.append("=&nbsp;");
            String changeE = "if( typeof(changeFilter) !='undefined')changeFilter(this);";
            try {
                String[] filterbydd = this.pageContext.getSession().getAttribute(attributeid + "_listfilterdd_" + filterbyColumn) != null ? StringUtil.split(this.pageContext.getSession().getAttribute(attributeid + "_listfilterdd_" + filterbyColumn).toString(), ";") : new String[]{};
                Arrays.sort(filterbydd, String.CASE_INSENSITIVE_ORDER);
                html.append("<select id=\"filterbycolumnsvalue\" name=\"filterbycolumnsvalue\" " + (this.browser != null && this.browser.isPhone() ? "style=\"font-size:16px\"" : "") + " onchange=\"if( typeof(changeFilter) !='undefined')changeFilter(this);\">\n");
                html.append("<option value=\"NONE\" ").append("(null)".equals(requestfilterbyValue) ? " selected>" : ">").append("").append("</option>");
                for (int i = 0; i < filterbydd.length; ++i) {
                    if (filterbydd[i].length() <= 0) continue;
                    html.append("<option value=\"").append(filterbydd[i]).append("\" ").append(filterbydd[i].equals(requestfilterbyValue) ? " selected>" : ">").append(tp.translate(filterbydd[i])).append("</option>");
                }
                html.append("</select>");
            }
            catch (Exception e) {
                html.append("<input id=\"filterbycolumnsvalue\" name=\"filterbycolumnsvalue\" " + (this.browser != null && this.browser.isPhone() ? "style=\"font-size:16px\"" : "") + " onchange=\"").append(changeE).append("\">\n");
            }
        }
        return html.toString();
    }

    @Override
    public String getHtml() {
        boolean useasfilter;
        String requestfilterbyValue;
        ConnectionProcessor cp = new ConnectionProcessor(this.pageContext);
        String userid = cp.getSapphireConnection().getSysuserId();
        ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(cp.getConnectionid());
        boolean filteringRendered = false;
        TranslationProcessor tp = this.getTranslationProcessor();
        if (this.element == null) {
            return tp.translate("No element data found for the sdilist tag.");
        }
        if (this.sdcProcessor == null) {
            this.sdcProcessor = this.getSDCProcessor();
        }
        this.sdcid = this.element.getProperty("sdcid");
        PropertyList sdc = (PropertyList)this.pageContext.getAttribute(this.sdcid + "_props");
        if (this.sdcid.length() > 0 && sdc == null) {
            sdc = this.sdcProcessor.getPropertyList(this.sdcid);
            this.pageContext.setAttribute(this.sdcid + "_props", (Object)sdc);
            if (sdc == null) {
                return "Can not find sdc definition for sdc:" + this.sdcid;
            }
            this.keycolid1 = sdc.getProperty("keycolid1");
            this.keycolid2 = sdc.getProperty("keycolid2");
            this.keycolid3 = sdc.getProperty("keycolid3");
            this.desccol = sdc.getProperty("desccol");
            this.setElementDefault(this.keycolid1, this.keycolid2, this.keycolid3, this.desccol);
        }
        StringBuffer html = new StringBuffer();
        html.append("\n<style>\n    .list_tablerowodd, .list_tableroweven {\n        height:" + this.element.getProperty("rowheight", "30") + "px;\n    }\n</style>\n");
        String command = this.pageContext.getRequest().getParameter("command");
        if (command == null) {
            command = "file";
        }
        PropertyList pagedata = null;
        try {
            pagedata = (PropertyList)this.pageContext.getRequest().getAttribute("pagedata");
        }
        catch (Exception exception) {
            // empty catch block
        }
        String pageorfile = command.equals("page") ? pagedata.getProperty("page") : this.pageContext.getRequest().getParameter("file");
        String attributeid = this.element != null && this.element.getProperty("attributenameaselementid", "N").equalsIgnoreCase("Y") ? this.elementid : (this.element != null && this.element.getProperty("listid").length() > 0 ? this.element.getProperty("listid") : pageorfile);
        String requestfilterbyColumn = pagedata != null ? pagedata.getProperty("filterby") : this.pageContext.getRequest().getParameter("filterby");
        String string = requestfilterbyValue = pagedata != null ? pagedata.getProperty("filterbyvalue") : this.pageContext.getRequest().getParameter("filterbyvalue");
        if (requestfilterbyColumn == null || requestfilterbyColumn.length() == 0) {
            requestfilterbyColumn = (String)this.pageContext.getSession().getAttribute(attributeid + "_listfilterby");
        }
        if (requestfilterbyColumn == null || requestfilterbyColumn.length() == 0) {
            requestfilterbyColumn = "NONE";
        }
        if (requestfilterbyValue == null || requestfilterbyValue.length() == 0) {
            requestfilterbyValue = (String)this.pageContext.getSession().getAttribute(attributeid + "_listfilterbyvalue");
        }
        if (requestfilterbyValue == null || requestfilterbyValue.length() == 0) {
            requestfilterbyValue = "(null)";
        }
        PropertyListCollection groupby = this.element.getCollection("groupby");
        PropertyList currentgroupby = null;
        PropertyList currentfilterby = null;
        String filterbyColumn = "";
        String filterbyValue = "";
        boolean grouping = false;
        PropertyList firstgroupby = null;
        boolean filtering = false;
        PropertyList firstfilterby = null;
        if (!"N".equals(this.element.getProperty("showgroupby")) && groupby != null && groupby.size() > 0) {
            for (int index = 0; index < groupby.size(); ++index) {
                useasfilter = groupby.getPropertyList(index).getProperty("useasfilter", "N").equalsIgnoreCase("Y");
                if (useasfilter) {
                    filtering = true;
                    firstfilterby = groupby.getPropertyList(index);
                    continue;
                }
                grouping = true;
                firstgroupby = groupby.getPropertyList(index);
            }
        }
        if (!"NONE".equals(requestfilterbyColumn) && filtering && firstfilterby != null) {
            if (requestfilterbyColumn == null || requestfilterbyColumn.length() == 0) {
                currentfilterby = firstfilterby;
                filterbyColumn = currentfilterby.getProperty("columnid");
                filterbyValue = "";
            } else {
                for (int g = 0; g < groupby.size(); ++g) {
                    String aliasReq;
                    currentfilterby = groupby.getPropertyList(g);
                    useasfilter = currentfilterby.getProperty("useasfilter", "N").equalsIgnoreCase("Y");
                    if (!useasfilter) continue;
                    String aliasCur = currentfilterby.getProperty("columnid").contains(" ") ? RequestParser.parseAlias(currentfilterby.getProperty("columnid")) : currentfilterby.getProperty("columnid");
                    String string2 = aliasReq = requestfilterbyColumn.contains(" ") ? RequestParser.parseAlias(requestfilterbyColumn) : requestfilterbyColumn;
                    if (!aliasReq.equals(aliasCur)) continue;
                    filterbyColumn = currentfilterby.getProperty("columnid");
                    break;
                }
                if (!filterbyColumn.equalsIgnoreCase("NONE")) {
                    filterbyValue = requestfilterbyValue;
                }
            }
            this.pageContext.getSession().setAttribute(attributeid + "_listfilterby", (Object)filterbyColumn);
            this.pageContext.getSession().setAttribute(attributeid + "_listfilterbyvalue", (Object)filterbyValue);
            if (filterbyColumn.indexOf(" ") > 0) {
                filterbyColumn = RequestParser.parseAlias(filterbyColumn);
            }
        } else {
            this.pageContext.getSession().setAttribute(attributeid + "_listfilterby", (Object)"NONE");
            this.pageContext.getSession().setAttribute(attributeid + "_listfilterbyvalue", (Object)"");
        }
        QueryData queryData = this.sdiInfo.getQueryData(this.datasetName);
        PropertyListCollection sortby = this.element.getCollection("sortby");
        PropertyListCollection columns = this.element.getCollectionNotNull("columns");
        ListMode listMode = ListMode.fromString(this.listmode);
        if (listMode.equals((Object)ListMode.SELECTABLE)) {
            listMode = ListMode.LIST;
        }
        StringBuilder changeListMode = new StringBuilder("<span id = 'change_list_mode'>");
        String elementListMode = this.element.getProperty("listmode", "selectable");
        if (elementListMode.equals("selectable")) {
            boolean canDoMap = false;
            this.mapHelper = new MapHelper(this.pageContext);
            canDoMap = this.mapHelper.checkCoordinateColumns(this.sdcid, columns, !listMode.equals((Object)ListMode.MAP));
            this.calendarHelper = new CalendarHelper(this.pageContext);
            boolean calendarConfigured = this.calendarHelper.calendarConfigured(this.element);
            if (listMode.equals((Object)ListMode.MAP) && !canDoMap) {
                listMode = ListMode.LIST;
            }
            if (elementListMode.equals("selectable")) {
                if (!listMode.equals((Object)ListMode.LIST)) {
                    changeListMode.append("&nbsp;<img id=\"changelistmode\" style=\"cursor:pointer; vertical-align:bottom\" src=\"").append(ListMode.LIST.getImage()).append("\" onclick=\"parent.changeListMode('list')\" title=\"").append(tp.translate("Change to list")).append("\"></img>");
                }
                if (calendarConfigured && !listMode.equals((Object)ListMode.CALENDAR)) {
                    changeListMode.append("&nbsp;<img id=\"changelistmode\" style=\"cursor:pointer; vertical-align:bottom\" src=\"").append(ListMode.CALENDAR.getImage()).append("\" onclick=\"parent.changeListMode('calendar')\" title=\"").append(tp.translate("Change to calendar")).append("\"></img>");
                }
                if (canDoMap && !listMode.equals((Object)ListMode.MAP)) {
                    changeListMode.append("&nbsp;<img id=\"changelistmode\" style=\"cursor:pointer; vertical-align:bottom\" src=\"").append(ListMode.MAP.getImage()).append("\" onclick=\"parent.changeListMode('map')\" title=\"").append(tp.translate("Change to map")).append("\"></img>");
                }
            }
        }
        changeListMode.append("</span>");
        String deferredColumnCode = "";
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList column = columns.getPropertyList(i);
            if (column == null) continue;
            String mode = column.getProperty("mode", "");
            if (mode.indexOf("$G{") == 0) {
                HashMap<String, Object> bindMap = new HashMap<String, Object>();
                bindMap.put("element", this.element);
                bindMap.put("pagedata", pagedata);
                bindMap.put("sdc", this.sdcProcessor.getSDCProperties(this.element.getProperty("sdcid")));
                bindMap.put("policy", new GroovyPolicyUtil(this.pageContext));
                if (this.connectionInfo != null) {
                    bindMap.put("user", this.connectionInfo.getUserAttributeMap());
                } else if (this.pageContext != null) {
                    bindMap.put("user", cp.getConnectionInfo(cp.getConnectionid()).getUserAttributeMap());
                }
                try {
                    mode = GroovyUtil.getInstance(this.pageContext).evaluateSecure(mode, bindMap);
                    column.setProperty("mode", mode);
                }
                catch (SapphireException se) {
                    Logger.logError(se.getMessage());
                }
            }
            if (mode.indexOf("Deferred Display") != 0) continue;
            deferredColumnCode = deferredColumnCode + ";" + ListAjaxRequest.registerDeferredColumn(column);
        }
        String sortbyColumn = "";
        String sortcallback = "";
        String lastsortby = (String)this.pageContext.getSession().getAttribute(attributeid + "_listsortby");
        if (!"Y".equals(this.overridepageorder)) {
            if (this.pageContext.getRequest().getParameter("sortby") != null && this.pageContext.getRequest().getParameter("sortby").length() > 0) {
                sortbyColumn = this.pageContext.getRequest().getParameter("sortby");
            } else if (this.tagsortby != null && this.tagsortby.length() > 0) {
                sortbyColumn = this.tagsortby;
            } else if (lastsortby != null && lastsortby.length() > 0) {
                sortbyColumn = lastsortby;
            }
            this.pageContext.getSession().setAttribute(attributeid + "_listsortby", (Object)sortbyColumn);
            String configsortbycolumns = "";
            if (sortby != null && sortby.size() > 0) {
                String dynamicSortbyid = sortbyColumn.indexOf(" ") > 0 ? sortbyColumn.substring(0, sortbyColumn.indexOf(" ")) : sortbyColumn;
                for (int i = 0; i < sortby.size(); ++i) {
                    String configsortbycolid = sortby.getPropertyList(i).getProperty("columnid");
                    configsortbycolid = "keycolid1".equals(configsortbycolid) ? this.keycolid1 : ("keycolid2".equals(configsortbycolid) ? this.keycolid2 : ("keycolid3".equals(configsortbycolid) ? this.keycolid3 : configsortbycolid));
                    String callback = sortby.getPropertyList(i).getProperty("callback");
                    if (callback != null && callback.length() > 0) {
                        sortcallback = callback;
                    }
                    if (configsortbycolid.length() <= 0 || configsortbycolid.equals(dynamicSortbyid)) continue;
                    String direction = sortby.getPropertyList(i).getProperty("asc_desc");
                    direction = direction != null && !direction.equals("d") ? "a" : "d";
                    configsortbycolumns = configsortbycolumns + (configsortbycolumns.length() > 0 ? "," : "") + configsortbycolid + " " + direction;
                }
            }
            if (sortbyColumn != null && sortbyColumn.length() > 0) {
                if (configsortbycolumns.length() > 0) {
                    sortbyColumn = sortbyColumn + "," + configsortbycolumns;
                }
            } else if (configsortbycolumns.length() > 0) {
                sortbyColumn = configsortbycolumns;
            }
            if (sortbyColumn == null || sortbyColumn.length() == 0) {
                sortbyColumn = "keycolid1";
            }
            if (sortbyColumn.indexOf("keycolid") >= 0) {
                sortbyColumn = StringUtil.replaceAll(sortbyColumn, "keycolid1", this.keycolid1);
                if (this.keycolid2 != null) {
                    sortbyColumn = StringUtil.replaceAll(sortbyColumn, "keycolid2", this.keycolid2);
                }
                if (this.keycolid3 != null) {
                    sortbyColumn = StringUtil.replaceAll(sortbyColumn, "keycolid3", this.keycolid3);
                }
            }
            this.element.setProperty("sortcallback", sortcallback);
        }
        String groupbyColumn = "";
        String groupbyTitle = "";
        PropertyList groupbyLink = null;
        String groupbyHref = "";
        String groupbyDisplay = "";
        String requestgroupbyColumn = this.pageContext.getRequest().getParameter("groupby");
        if (!listMode.equals((Object)ListMode.LIST)) {
            requestgroupbyColumn = "NONE";
        }
        if (requestgroupbyColumn == null || requestgroupbyColumn.length() == 0) {
            requestgroupbyColumn = (String)this.pageContext.getSession().getAttribute(attributeid + "_listgroupby");
        }
        if (OpalUtil.isEmpty(requestgroupbyColumn)) {
            try {
                requestgroupbyColumn = configurationProcessor.getProfileProperty(userid, attributeid + PROPERTY_LIST_GROUPBY);
            }
            catch (SapphireException exp) {
                this.logger.info("Error getting profile property : " + attributeid + PROPERTY_LIST_GROUPBY);
                requestgroupbyColumn = null;
            }
        }
        if ((requestgroupbyColumn == null || requestgroupbyColumn.length() == 0) && "N".equals(this.element.getProperty("initialgrouped"))) {
            requestgroupbyColumn = "NONE";
        }
        if (!"NONE".equals(requestgroupbyColumn) && grouping && firstgroupby != null) {
            if (requestgroupbyColumn == null || requestgroupbyColumn.length() == 0) {
                currentgroupby = firstgroupby;
                groupbyColumn = currentgroupby.getProperty("columnid");
            } else {
                for (int g = 0; g < groupby.size(); ++g) {
                    String aliasReq;
                    currentgroupby = groupby.getPropertyList(g);
                    boolean useasfilter2 = currentgroupby.getProperty("useasfilter", "N").equalsIgnoreCase("Y");
                    if (useasfilter2) continue;
                    String aliasCur = currentgroupby.getProperty("columnid").contains(" ") ? RequestParser.parseAlias(currentgroupby.getProperty("columnid")) : currentgroupby.getProperty("columnid");
                    String string3 = aliasReq = requestgroupbyColumn.contains(" ") ? RequestParser.parseAlias(requestgroupbyColumn) : requestgroupbyColumn;
                    if (!aliasReq.equals(aliasCur)) continue;
                    groupbyColumn = currentgroupby.getProperty("columnid");
                    break;
                }
            }
            this.pageContext.getSession().setAttribute(attributeid + "_listgroupby", (Object)groupbyColumn);
            try {
                configurationProcessor.setProfileProperty(userid, attributeid + PROPERTY_LIST_GROUPBY, groupbyColumn);
            }
            catch (SapphireException exp) {
                this.logger.info("Error setting profile property : " + attributeid + PROPERTY_LIST_GROUPBY);
            }
            if (groupbyColumn.indexOf(" ") > 0) {
                groupbyColumn = RequestParser.parseAlias(groupbyColumn);
            }
            groupbyTitle = currentgroupby.getProperty("title");
            groupbyDisplay = currentgroupby.getProperty("displayvalue");
            groupbyLink = currentgroupby.getPropertyList("link");
            groupbyHref = groupbyLink != null ? groupbyLink.getProperty("href") : "";
            String firstsortbycolid = "";
            if (sortbyColumn != null && sortbyColumn.length() > 0) {
                firstsortbycolid = sortbyColumn.indexOf(" a") > 0 ? sortbyColumn.substring(0, sortbyColumn.indexOf(" a")) : (sortbyColumn.indexOf(" d") > 0 ? sortbyColumn.substring(0, sortbyColumn.indexOf(" d")) : (sortbyColumn.indexOf(",") > 0 ? sortbyColumn.substring(0, sortbyColumn.indexOf(",")) : sortbyColumn));
            }
            String sortwithgroupby = groupbyColumn + "," + sortbyColumn;
            if (groupbyColumn.equals(firstsortbycolid)) {
                sortwithgroupby = sortbyColumn;
            }
            sortwithgroupby = this.addDisplaySortColumns(sortwithgroupby, queryData);
            String newsortby = List.getForceNumericSortString(sortwithgroupby, this.keycolid2.indexOf("versionid") > 0 ? this.keycolid2 : null);
            queryData.resetGroup(newsortby, groupbyColumn);
        } else {
            this.pageContext.getSession().setAttribute(attributeid + "_listgroupby", (Object)"NONE");
            try {
                configurationProcessor.setProfileProperty(userid, attributeid + PROPERTY_LIST_GROUPBY, "NONE");
            }
            catch (SapphireException exp) {
                this.logger.info("Error setting profile property : " + attributeid + PROPERTY_LIST_GROUPBY);
            }
            queryData.resetGroup("", "");
            if (sortbyColumn.length() > 0) {
                sortbyColumn = this.addDisplaySortColumns(sortbyColumn, queryData);
                String newsortby = List.getForceNumericSortString(sortbyColumn, this.keycolid2.indexOf("versionid") > 0 ? this.keycolid2 : null);
                queryData.sort(newsortby);
            }
            this.element.setProperty("initexpandimg", "WEB-CORE/elements/images/minus.gif");
            this.element.setProperty("initexpandstyle", "display:block;");
        }
        DataSet beforeFilter = (DataSet)queryData.getQuerydata().clone();
        StringBuffer linkhtml = null;
        StringBuffer pagestatus = new StringBuffer();
        StringBuffer pageselector = new StringBuffer();
        boolean isPagingRequest = false;
        String pageNo = this.pageContext.getRequest().getParameter("pageno");
        int currentpage = 1;
        int totalrows = queryData.getRowCount();
        int qualifiedrows = this.sdiInfo.getSDIData() != null ? this.sdiInfo.getSDIData().getQualifiedRows() : totalrows;
        int rowsperpage = 0;
        String sm = this.pageContext.getRequest().getParameter("selectionmode");
        if (sm == null || sm.length() == 0) {
            sm = this.element.getProperty("selectiontype", "Prompt");
        }
        try {
            if (this.element.getProperty("rowsperpage").length() > 0) {
                rowsperpage = Integer.parseInt(this.element.getProperty("rowsperpage"));
            }
            if (pageNo != null && pageNo.length() > 0) {
                isPagingRequest = true;
                currentpage = Integer.parseInt(pageNo);
            }
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        if (isPagingRequest) {
            totalrows = Integer.parseInt(this.pageContext.getRequest().getParameter("totalrows"));
            qualifiedrows = Integer.parseInt(this.pageContext.getRequest().getParameter("qualifiedrows"));
        }
        StringBuffer paginghtml = this.getPagingHTMLStart(sortbyColumn, groupbyColumn, filterbyColumn, filterbyValue, isPagingRequest, command, currentpage, pageorfile, totalrows, rowsperpage, qualifiedrows, queryData);
        boolean showScanFindSelect = "Y".equals(this.element.getProperty("showscanfindselect"));
        boolean hideSelectedCount = this.pageContext.getRequest().getParameter("hideselectedcount") != null && this.pageContext.getRequest().getParameter("hideselectedcount").equals("Y");
        String selectedcount = "<div class=\"list_selectedcount\" style=\"display:" + ("checkbox".equals(this.element.getProperty("selectortype")) || "radiobutton".equals(this.element.getProperty("selectortype")) || "".equals(this.element.getProperty("selectortype")) ? "inline" : "none") + "\">&nbsp;[&nbsp;<span id=\"selectedcount\">0</span>&nbsp;" + tp.translate("selected") + "&nbsp;]&nbsp;" + (showScanFindSelect ? "<img id=\"scanfindselectdiv\" style=\"vertical-align:bottom\" src=\"WEB-CORE/imageref/flat/16/flat_black_barcode2.svg\" onclick=\"listlayout.openScanFindPopup()\" title=\"" + tp.translate("Scan find and select") + "\"></img>" : "") + "</div>";
        int totalpage = 1;
        if (rowsperpage > 0) {
            totalpage = totalrows / rowsperpage;
            if (totalrows % rowsperpage > 0) {
                ++totalpage;
            }
            int maxnumpagelink = currentpage <= 5 ? 10 : 11;
            int numprelinks = maxnumpagelink / 2;
            int numpostlinks = maxnumpagelink / 2 - (maxnumpagelink % 2 == 0 ? 1 : 0);
            String prev = tp.translate(ElementUtil.getText(this.element, "prev", "Prev"));
            String next = tp.translate(ElementUtil.getText(this.element, "next", "Next"));
            String page = tp.translate(ElementUtil.getText(this.element, "page", "page"));
            String of = tp.translate(ElementUtil.getText(this.element, "of", "of"));
            if (currentpage == 0) {
                currentpage = 1;
            }
            linkhtml = new StringBuffer();
            boolean showSelector = false;
            if (showSelector) {
                pageselector = new StringBuffer("<select onchange=\"getPage( this.value )\">");
            }
            int pagenum = 1;
            int count = 0;
            int firstpageno = -1;
            int lastpageno = -1;
            for (int i = 0; i < totalrows; ++i) {
                if (++count % rowsperpage != 0 && i != totalrows - 1) continue;
                boolean pagenoUsed = false;
                if (showSelector) {
                    pageselector.append("<option value=\"").append(pagenum).append("\" ").append(pagenum == currentpage ? "selected" : "").append(">").append(pagenum).append("</option>");
                }
                if (totalpage > 1) {
                    if (pagenum == currentpage) {
                        linkhtml.append("&nbsp;").append("<b>" + pagenum + "</b>").append("&nbsp;");
                        pagenoUsed = true;
                    } else if (!(pagenum < currentpage - numprelinks && totalpage - pagenum >= maxnumpagelink || pagenum > currentpage + numpostlinks && pagenum > maxnumpagelink)) {
                        linkhtml.append("&nbsp;<a style=\"text-decoration:none\" href=\"Javascript:getPage( '").append(pagenum).append("' )\" title=\"").append((pagenum - 1) * rowsperpage + 1).append("-").append(pagenum * rowsperpage < totalrows ? pagenum * rowsperpage : totalrows).append("\">").append(pagenum).append("</a>&nbsp;");
                        pagenoUsed = true;
                    }
                }
                if (pagenoUsed) {
                    if (firstpageno < 0) {
                        firstpageno = pagenum;
                    }
                    lastpageno = pagenum;
                }
                ++pagenum;
            }
            if (totalpage > 1) {
                if (currentpage != 1) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("<a style=\"text-decoration:none\" href=\"Javascript:getPage( '").append(currentpage - 1).append("' )\" title=\"").append(prev).append("\">").append(prev).append("</a>&nbsp;");
                    sb.append("<img onclick=\"Javascript:getPage( '").append(currentpage - 1).append("' )\" src=\"WEB-CORE/elements/images/arwleft.gif\" title=\"").append(prev).append("\"/>");
                    if (firstpageno > 1) {
                        pagenum = 1;
                        sb.append("&nbsp;<a style=\"text-decoration:none\" href=\"Javascript:getPage( '").append(pagenum).append("' )\" title=\"").append((pagenum - 1) * rowsperpage + 1).append("-").append(pagenum * rowsperpage < totalrows ? pagenum * rowsperpage : totalrows).append("\">").append(pagenum).append("</a>&nbsp;");
                        if (firstpageno > 2) {
                            sb.append("...");
                        }
                    }
                    linkhtml.insert(0, sb);
                }
                if (totalpage - lastpageno > 0) {
                    if (totalpage - lastpageno > 1) {
                        linkhtml.append("...");
                    }
                    pagenum = totalpage;
                    linkhtml.append("&nbsp;<a style=\"text-decoration:none\" href=\"Javascript:getPage( '").append(pagenum).append("' )\" title=\"").append((pagenum - 1) * rowsperpage + 1).append("-").append(pagenum * rowsperpage < totalrows ? pagenum * rowsperpage : totalrows).append("\">").append(pagenum).append("</a>&nbsp;");
                }
                if (currentpage != lastpageno) {
                    linkhtml.append("&nbsp;<img onclick=\"Javascript:getPage(").append(currentpage + 1).append(")\" src=\"WEB-CORE/elements/images/arwrt.gif\" title=\"").append(next).append("\"/>&nbsp;");
                    linkhtml.append("<a style=\"text-decoration:none\" href=\"Javascript:getPage(").append(currentpage + 1).append(")\" title=\"").append(next).append("\">").append(next).append("</a>&nbsp;");
                }
            }
            if (showSelector) {
                pageselector.append("</select>");
            }
            html.append("<table class=\"list_pagingtable_top\"><tr><td>");
            html.append(selectedcount);
            pagestatus.append(totalrows == 0 ? 0 : (currentpage - 1) * rowsperpage + 1).append(" - ").append(currentpage * rowsperpage < totalrows ? currentpage * rowsperpage : totalrows).append(" ").append(of).append(" ").append(totalrows);
            html.append("<div nowrap class=\"list_currentpageinfo_top\">");
            if (showSelector) {
                pagestatus.append(" ( ").append(page).append(" ").append(pageselector).append(" ").append(of).append(" ").append(totalpage).append(" )");
            }
            html.append(pagestatus);
            if (listMode.equals((Object)ListMode.CALENDAR)) {
                html.append("<span nowrap id='calendar_date_range'></span>");
            }
            html.append((CharSequence)changeListMode);
            html.append("</div>");
        } else {
            boolean hidegroupingtopheader = this.element.getProperty("hidegroupingtopheader", "N").equalsIgnoreCase("Y");
            if (!hideSelectedCount || grouping) {
                html.append("<table class=\"list_pagingtable_top\" style=\"display:" + (hidegroupingtopheader ? "none" : "inline") + "\"><tr><td>");
                html.append("<div class=\"list_currentpageinfo_top\">").append(selectedcount).append(totalrows).append(" ").append(tp.translate("Total"));
                if ("Y".equals(this.element.getProperty("showgroupcount")) && grouping && !"NONE".equals(requestgroupbyColumn)) {
                    html.append(" " + tp.translate("in") + " <span id=\"groupcount\">..</span> ").append(tp.translate("Groups")).append("</div>");
                }
                html.append((CharSequence)changeListMode);
                html.append("</div>");
            } else {
                html.append("<table class=\"list_pagingtable_top\"><tr><td>");
            }
        }
        if (qualifiedrows > totalrows) {
            html.append("<span class=\"list_rowlimitinfo\">&nbsp;(").append(totalrows).append(" ").append(tp.translate("of")).append(" ").append(qualifiedrows).append(")</span>");
        }
        if (this.browser != null && this.browser.isPhone()) {
            html.append("</td></tr><tr><td style=\"white-space:nowrap\">");
        }
        html.append("<div style=\"white-space:nowrap; margin-left:" + (this.browser != null && this.browser.isPhone() ? "0" : "50") + "px\" class=\"list_selectall_top\">");
        if (grouping || filtering) {
            html.append("<span>");
        }
        if (listMode.equals((Object)ListMode.CALENDAR)) {
            this.calendarHelper = new CalendarHelper(this.pageContext);
            html.append(this.calendarHelper.getDatesSelector(this.element, this.getTranslationProcessor()));
        }
        StringBuilder buf = new StringBuilder();
        if (grouping && listMode.equals((Object)ListMode.LIST)) {
            buf.append(ElementUtil.getText(this.element, "groupby", "Group By", tp)).append("&nbsp;<select id=\"groupbycolumns\" name=\"groupbycolumns\" " + (this.browser != null && this.browser.isPhone() ? "style=\"font-size:16px\"" : "") + " onchange=\"pagingform.groupby.value=this.value;pagingform.submit();\">\n");
            buf.append("<option value=\"NONE\" ").append("NONE".equals(requestgroupbyColumn) ? "selected>" : ">").append(tp.translate("None"));
            for (int index = 0; index < groupby.size(); ++index) {
                PropertyList plGroupBy = groupby.getPropertyList(index);
                boolean useasfilter3 = plGroupBy.getProperty("useasfilter", "N").equalsIgnoreCase("Y");
                if (useasfilter3) continue;
                String aliasCurr = plGroupBy.getProperty("columnid").contains(" ") ? RequestParser.parseAlias(plGroupBy.getProperty("columnid")) : plGroupBy.getProperty("columnid");
                String aliasReq = groupbyColumn.contains(" ") ? RequestParser.parseAlias(groupbyColumn) : groupbyColumn;
                buf.append("<option value=\"").append(plGroupBy.getProperty("columnid")).append("\"").append(aliasCurr.equals(aliasReq) ? "selected>" : ">").append(SafeHTML.encodeForHTML(plGroupBy.getProperty("title"), true)).append("</option>");
            }
            buf.append("</select>");
        }
        if (filtering) {
            html.append(this.getFilterHTML(tp, requestfilterbyColumn, requestfilterbyValue, groupby, filterbyColumn, currentfilterby, attributeid));
            filteringRendered = true;
        }
        if (buf.length() > 0) {
            if (filtering) {
                html.append("&nbsp;");
            }
            html.append((CharSequence)buf);
        }
        if (grouping || filtering) {
            html.append("&nbsp;&nbsp;&nbsp;");
            html.append("</span>");
        }
        html.append("</div>");
        if (linkhtml != null && linkhtml.length() > 0) {
            if ("checkbox".equals(this.element.getProperty("selectortype")) || this.element.getProperty("selectortype").length() == 0) {
                if (this.element.getProperty("selectiontype", "Prompt").equalsIgnoreCase("Prompt")) {
                    if (sm.equalsIgnoreCase("Prompt")) {
                        sm = "All Pages";
                    }
                    html.append(tp.translate("Selection Mode") + " ").append("<select name=\"__selectMode\" id=\"__selectMode\" " + (this.browser != null && this.browser.isPhone() ? " style=\"font-size:16px\"" : "") + " onChange=\"top.sapphire.page.list.setSelectionMode(this.value);\"><option value=\"All Pages\"").append(sm.equalsIgnoreCase("All Pages") ? " SELECTED" : "").append(">").append(tp.translate("All Pages")).append("</option><option value=\"Current Page\"").append(sm.equalsIgnoreCase("Current Page") ? " SELECTED" : "").append(">").append(tp.translate("Current Page")).append("</option></select>");
                } else if (this.element.getProperty("selectiontype", "Prompt").equalsIgnoreCase("Hidden")) {
                    sm = "All Pages";
                } else {
                    html.append(tp.translate("Selection Mode:") + " ").append(tp.translate(sm));
                }
            } else {
                sm = "Current Page";
            }
            if (this.browser != null && this.browser.isPhone()) {
                html.append("</td></tr><tr><td>");
            }
            html.append("&nbsp;<span" + (this.browser != null && this.browser.isPhone() ? " style=\"font-size:16px\"" : "") + ">" + linkhtml + "</span>");
        }
        html.append("</td>");
        boolean showCollapseAll = false;
        if (groupby != null && groupby.size() > 0 && !"NONE".equals(requestgroupbyColumn)) {
            showCollapseAll = true;
        }
        html.append("</tr></table>");
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/list.js\"></script>\n");
        if (listMode.equals((Object)ListMode.MAP)) {
            PropertyListCollection iconStyles = this.element.getPropertyListNotNull("mapprops").getCollectionNotNull("iconstyles");
            String extraColumnConf = this.element.getPropertyListNotNull("mapprops").getProperty("fetchextracolumns", "");
            this.mapHelper = new MapHelper(this.pageContext);
            if (!extraColumnConf.isEmpty()) {
                html.append(this.mapHelper.setExtraColumns(this.sdiInfo, extraColumnConf, this.element));
            }
            html.append(this.mapHelper.getHtml(iconStyles));
        }
        if (listMode.equals((Object)ListMode.CALENDAR)) {
            this.calendarHelper = new CalendarHelper(this.pageContext);
            html.append(this.calendarHelper.getHtml(this.element));
            M18NUtil m18NUtil = new M18NUtil(this.pageContext);
            html.append((CharSequence)DHTMLXScheduler.getSchedulerTranslationScript(m18NUtil, this.getTranslationProcessor()));
        }
        if (this.sdiInfo.getRowCount(this.datasetName) > 0 || listMode.equals((Object)ListMode.CALENDAR)) {
            if (this.element.getProperty("timezoneoffset").length() > 0) {
                this.sdiInfo.getDataSet(this.datasetName).setTimezoneOffset(this.element.getProperty("timezoneoffset"));
            }
            int groupcount = 0;
            do {
                ++groupcount;
                ListTable table = new ListTable(this.pageContext, this.sdiInfo, this.sdcProcessor);
                table.setListmode(listMode);
                if (listMode.equals((Object)ListMode.LIST) && !"NONE".equals(requestgroupbyColumn) && groupbyColumn.length() > 0 && groupby != null && groupby.size() > 0) {
                    String translatevalue;
                    String selectorType;
                    StringBuffer groupheaderRowHtml = new StringBuffer();
                    String groupbyValue = this.sdiInfo.getValue(this.datasetName, groupbyColumn);
                    String groupName = groupbyValue + "group" + groupcount;
                    groupheaderRowHtml.append("<tr id=\"" + SafeHTML.encodeForHTMLAttribute(groupName) + "groupheaderrow\" class=\"list_grouptitle\">\n");
                    groupheaderRowHtml.append("<td class=\"list_grouptitle\">\n");
                    groupheaderRowHtml.append("<img src=\"").append(this.element.getProperty("initexpandimg").length() == 0 ? "WEB-CORE/pagetypes/list/images/minus.gif" : this.element.getProperty("initexpandimg")).append("\" id=\"").append(SafeHTML.encodeForHTMLAttribute(groupName)).append("\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\"/>");
                    if (!(this.element.getProperty("selectortype").equals("hyperlink") && this.element.getProperty("selectortype").equals("none") || (selectorType = this.element.getProperty("selectortype")) != null && (selectorType.equals("none") || selectorType.equals("radiobutton")))) {
                        groupheaderRowHtml.append("<input id=\"headerselector\" type=\"checkbox\" style=\"margin-left:6px\" name=\"").append(SafeHTML.encodeForHTMLAttribute(groupName)).append("\" onclick=\"setallfor( '").append(SafeHTML.encodeForJavaScript(groupName)).append("', this.checked );\"").append(this.element.get("checkedclause")).append("/>&nbsp;\n");
                    }
                    groupheaderRowHtml.append("</td>");
                    groupheaderRowHtml.append("<td colspan=\"42\" class=\"list_grouptitle\">\n").append(SafeHTML.encodeForHTML(groupbyTitle, true)).append("&nbsp;");
                    String groupbyDValue = SDITagUtil.getDisplayValue(groupbyValue, groupbyDisplay);
                    String string4 = translatevalue = currentgroupby != null ? currentgroupby.getProperty("translatevalue") : "";
                    if ("Y".equals(translatevalue)) {
                        groupbyDValue = tp.translate(groupbyDValue);
                    }
                    if (groupbyLink != null && groupbyLink.size() > 0 && groupbyHref != null && groupbyHref.trim().length() > 0) {
                        String evalgroupbyLink = ElementUtil.evaluateExpression(-1, groupbyColumn, groupbyHref, this.sdiInfo);
                        String target = groupbyLink.getProperty("target").length() > 0 ? groupbyLink.getProperty("target") : "_top";
                        groupheaderRowHtml.append("<a href=\"").append(evalgroupbyLink).append("\" target=\"").append(target).append("\"");
                        if (groupbyLink.getProperty("tip") != null && groupbyLink.getProperty("tip").length() > 0) {
                            groupheaderRowHtml.append(" title=\"").append(groupbyLink.getProperty("tip")).append("\"");
                        }
                        groupheaderRowHtml.append(">").append(SafeHTML.encodeForHTML(groupbyDValue, true)).append("</a>\n");
                    } else {
                        groupheaderRowHtml.append(SafeHTML.encodeForHTML(groupbyDValue, true));
                    }
                    PropertyListCollection groupbyaddcols = currentgroupby != null ? currentgroupby.getCollection("columns") : null;
                    for (int col = 0; col < (groupbyaddcols == null ? 0 : groupbyaddcols.size()); ++col) {
                        String colid = groupbyaddcols.getPropertyList(col).getProperty("columnid");
                        if (colid.indexOf(" ") > 0) {
                            colid = RequestParser.parseAlias(colid);
                        }
                        String value = this.sdiInfo.getValue(this.datasetName, colid);
                        String display = groupbyaddcols.getPropertyList(col).getProperty("displayvalue");
                        value = SDITagUtil.getDisplayValue(value, display);
                        translatevalue = groupbyaddcols.getPropertyList(col).getProperty("translatevalue");
                        if ("Y".equals(translatevalue)) {
                            value = tp.translate(value);
                        }
                        groupheaderRowHtml.append("&nbsp;").append(groupbyaddcols.getPropertyList(col).getProperty("title")).append("&nbsp;").append(SafeHTML.encodeForHTML(value, true));
                    }
                    groupheaderRowHtml.append("[count]");
                    groupheaderRowHtml.append("</td>\n");
                    groupheaderRowHtml.append("</tr>\n");
                    table.setShowCollapseAll(showCollapseAll);
                    table.setGroupHeaderRowHtml(groupheaderRowHtml);
                }
                table.setRenderHeader(groupcount == 1);
                table.setDatasetName(this.datasetName);
                table.setElementProperties(this.element);
                table.setSortbyColumn(sortbyColumn.indexOf(",") > 0 ? sortbyColumn.substring(0, sortbyColumn.indexOf(",")) : sortbyColumn);
                html.append(table.getHtml());
            } while (queryData.nextGroup(-1));
            html.append("</tbody>");
            html.append("</table>");
            if (!"N".equals(this.element.getProperty("enablefixedheader"))) {
                html.append("</div>\n");
            }
            String returncols = (this.pageContext.getAttribute(this.element.getId() + "_returncolidlist") != null ? this.pageContext.getAttribute(this.element.getId() + "_returncolidlist") : ";").toString().substring(1);
            String rowcols = (this.pageContext.getAttribute(this.element.getId() + "_rowcolidlist") != null ? this.pageContext.getAttribute(this.element.getId() + "_rowcolidlist") : ";").toString().substring(1);
            String cols = (this.pageContext.getAttribute(this.element.getId() + "_columnidlist") != null ? this.pageContext.getAttribute(this.element.getId() + "_columnidlist") : ";").toString().substring(1);
            if (paginghtml != null) {
                paginghtml.append(this.getPagingHTMLEnd(returncols, rowcols, beforeFilter, pagedata));
                html.append(paginghtml.toString());
            }
            html.append("<script>\n");
            html.append("if( document.getElementById('groupcount') != null ) document.getElementById('groupcount').innerHTML='" + groupcount + "';\n");
            html.append("window.onload = new Function('setDataLoaded(\"").append(this.element.getProperty("selectortype", "checkbox")).append("\")');\n");
            html.append("columnlistmap['").append(this.element.getId()).append("_keycolidlist']='").append(this.keycolid1).append(this.keycolid2.length() == 0 ? "" : ";" + this.keycolid2).append(this.keycolid3.length() == 0 ? "" : ";" + this.keycolid3).append("';\n");
            html.append("columnlistmap['").append(this.element.getId()).append("_returncolidlist']='").append(returncols).append("';\n");
            html.append("columnlistmap['").append(this.element.getId()).append("_columnidlist']='").append(cols).append("';\n");
            html.append("columnlistmap['").append(this.element.getId()).append("_rowcolidlist']='").append(rowcols).append("';\n");
            html.append("columnlistmap['").append(this.element.getId()).append("_desccollist']='").append(this.desccol).append("';\n");
            String selectall = ElementUtil.getText(this.element, "selectall", "Select All", tp);
            String deselectall = ElementUtil.getText(this.element, "deselectall", "Deselect All", tp);
            html.append("_deselectall_text = '").append(deselectall).append("';\n");
            html.append("_selectall_text = '").append(selectall).append("';\n");
            html.append("parent.sapphire.page.list.setSelectionMode('").append(sm).append("');\n");
            if (deferredColumnCode.length() > 0) {
                html.append("var deferredColumnCode='" + deferredColumnCode.substring(1) + "';\n");
            }
            html.append("var list_scrollheight='" + this.element.getProperty("height") + "';\n");
            html.append("var initexpand = " + !"display:none;".equals(this.element.getProperty("initexpandstyle")) + ";\n");
            if (!this.element.getProperty("disablelistlayout", "N").equalsIgnoreCase("Y")) {
                html.append("sapphire.gwt.addGWTElement( \"listlayout\", \"listlayout\", {} );\n");
            }
            html.append("</script>");
        } else {
            if (filtering && !filteringRendered) {
                html.append("<div style=\"\" class=\"list_norows_top\">");
                html.append("<span>");
                html.append(this.getFilterHTML(tp, requestfilterbyColumn, requestfilterbyValue, groupby, filterbyColumn, currentfilterby, attributeid));
                html.append("&nbsp;&nbsp;&nbsp;");
                html.append("</span>");
                html.append("</div>");
            }
            html.append("<p style=\"margin-left:195px\" id=\"norowsfound\">").append(ElementUtil.getText(this.element, "norowsfound", tp.translate("No rows found."))).append("</p>\n");
        }
        return html.toString();
    }

    private String addDisplaySortColumns(String sortwithgroupby, QueryData queryData) {
        PropertyListCollection columns = this.element.getCollection("columns");
        HashSet<String> transcolset = new HashSet<String>();
        HashMap<String, String> colidDisplayvalue = new HashMap<String, String>();
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList column = columns.getPropertyList(i);
            if (!"Yes".equals(column.getProperty("translatevalue"))) continue;
            String datasetcolid = RequestParser.parseAlias(column.getProperty("columnid"));
            transcolset.add(RequestParser.parseAlias(column.getProperty("columnid")));
            if (column.getProperty("displayvalue").length() <= 0) continue;
            colidDisplayvalue.put(datasetcolid, column.getProperty("displayvalue"));
        }
        if (transcolset.size() > 0) {
            String[] sortbys = StringUtil.split(sortwithgroupby, ",");
            ArrayList<String> transcollist = new ArrayList<String>();
            for (int i = 0; i < sortbys.length; ++i) {
                String sortbyColumnid = sortbys[i];
                if (sortbyColumnid.indexOf(" a") > 0) {
                    sortbyColumnid = sortbyColumnid.substring(0, sortbyColumnid.indexOf(" a")).trim();
                } else if (sortbyColumnid.indexOf(" d") > 0) {
                    sortbyColumnid = sortbyColumnid.substring(0, sortbyColumnid.indexOf(" d")).trim();
                }
                if (!transcolset.contains(sortbyColumnid)) continue;
                transcollist.add(sortbyColumnid);
                sortbys[i] = sortbys[i].replaceFirst(sortbyColumnid, sortbyColumnid + "_display");
            }
            if (transcollist.size() > 0) {
                int i;
                DataSet ds = queryData.getQuerydata();
                int totalrow = ds.getRowCount();
                for (i = 0; i < transcollist.size(); ++i) {
                    String colid = (String)transcollist.get(i);
                    String displaycolid = colid + "_display";
                    ds.addColumn(displaycolid, 0);
                    for (int row = 0; row < totalrow; ++row) {
                        String value = ds.getValue(row, colid);
                        if (colidDisplayvalue.get(colid) != null) {
                            value = SDITagUtil.getDisplayValue(value, (String)colidDisplayvalue.get(colid));
                        }
                        ds.setValue(row, displaycolid, this.getTranslationProcessor().translate(value));
                    }
                }
                for (i = 0; i < sortbys.length; ++i) {
                    sortwithgroupby = i == 0 ? sortbys[i] : sortwithgroupby + "," + sortbys[i];
                }
            }
        }
        return sortwithgroupby;
    }

    private void setElementDefault(String keycolid1, String keycolid2, String keycolid3, String desccol) {
        PropertyListCollection columns = this.element.getCollection("columns");
        if (columns == null) {
            columns = new PropertyListCollection();
            this.element.setProperty("columns", columns);
        }
        ElementUtil.setColumnDateDisplayFormat(this.pageContext, this.element.getCollection("columns"), this.datasetName, this.sdiInfo, (PropertyList)this.pageContext.getAttribute(this.sdcid + "_props"));
        ElementUtil.setColumnDisplayValue(this.pageContext, this.element.getCollection("columns"), (PropertyList)this.pageContext.getAttribute(this.sdcid + "_props"), this.getTranslationProcessor());
        PropertyListCollection groupbyColumns = this.element.getCollectionNotNull("groupby");
        block0: for (int i = 0; i < groupbyColumns.size(); ++i) {
            String columnid = groupbyColumns.getPropertyList(i).getProperty("columnid");
            for (int c = 0; c < columns.size(); ++c) {
                if (!columnid.equals(columns.getPropertyList(c).getProperty("columnid"))) continue;
                groupbyColumns.getPropertyList(i).setProperty("reftypeid", columns.getPropertyList(c).getProperty("reftypeid"));
                continue block0;
            }
        }
        ElementUtil.setColumnDisplayValue(this.pageContext, this.element.getCollection("groupby"), (PropertyList)this.pageContext.getAttribute(this.sdcid + "_props"), this.getTranslationProcessor());
        if (columns == null || columns.size() == 0) {
            columns = new PropertyListCollection();
            PropertyList keycol1 = new PropertyList();
            keycol1.setProperty("columnid", keycolid1);
            columns.add(keycol1);
            if (keycolid2 != null && keycolid2.length() > 0) {
                PropertyList keycol2 = new PropertyList();
                keycol2.setProperty("columnid", keycolid2);
                columns.add(keycol2);
                if (keycolid3 != null && keycolid3.length() > 0) {
                    PropertyList keycol3 = new PropertyList();
                    keycol3.setProperty("columnid", keycolid3);
                    columns.add(keycol3);
                }
            }
            PropertyList desccolumn = new PropertyList();
            desccolumn.setProperty("columnid", desccol);
            columns.add(desccolumn);
            this.element.put("columns", columns);
        }
        if (columns != null && columns.size() > 0) {
            ArrayList<PropertyList> toberemoved = new ArrayList<PropertyList>();
            for (int i = 0; i < columns.size(); ++i) {
                PropertyList column = columns.getPropertyList(i);
                String columnid = column.getProperty("columnid");
                if (columnid.equals("keycolid1")) {
                    column.setProperty("columnid", keycolid1);
                    continue;
                }
                if (columnid.equals("keycolid2")) {
                    if (keycolid2 != null && keycolid2.length() > 0) {
                        column.setProperty("columnid", keycolid2);
                        continue;
                    }
                    toberemoved.add(column);
                    continue;
                }
                if (columnid.equals("keycolid3")) {
                    if (keycolid3 != null && keycolid3.length() > 0) {
                        column.setProperty("columnid", keycolid3);
                        continue;
                    }
                    toberemoved.add(column);
                    continue;
                }
                if (!columnid.equals("desccolid") && !columnid.equals("desccol")) continue;
                if (desccol != null && desccol.length() > 0) {
                    column.setProperty("columnid", desccol);
                    continue;
                }
                toberemoved.add(column);
            }
            if (toberemoved.size() > 0) {
                columns.removeAll(toberemoved);
            }
        }
        String checkedclause = "";
        this.element.setProperty("checkedclause", checkedclause);
        String initexpandall = this.element.getProperty("initexpandall");
        if (initexpandall != null && initexpandall.equals("N") && this.element.getCollection("groupby") != null && this.element.getCollection("groupby").size() > 0) {
            this.element.setProperty("initexpandimg", "WEB-CORE/elements/images/plus.gif");
            this.element.setProperty("initexpandstyle", "display:none;");
        } else {
            this.element.setProperty("initexpandimg", "WEB-CORE/elements/images/minus.gif");
            this.element.setProperty("initexpandstyle", "display:block;");
        }
        ElementUtil.setColumnDefaultTitle(this.element.getCollection("columns"), ((PropertyList)this.pageContext.getAttribute(this.sdcid + "_props")).getCollection("columns"), this.getTranslationProcessor());
    }

    private static String getForceNumericSortString(String sortbycolumns, String forceNumericSortColumnid) {
        if (forceNumericSortColumnid != null && forceNumericSortColumnid.length() > 0 && sortbycolumns.indexOf(forceNumericSortColumnid) >= 0) {
            sortbycolumns = StringUtil.replaceAll(sortbycolumns, forceNumericSortColumnid + " ", forceNumericSortColumnid + " n");
        }
        return sortbycolumns;
    }

    public static void mergeRequestOverrides(PropertyList pagedata, PropertyList listElement, PropertyList toolbarElement, PropertyList searchbarElement, PropertyList layout, PropertyList requestOverrides, boolean isListContent) {
        List.mergeRequestOverrides(pagedata, listElement, toolbarElement, searchbarElement, layout, requestOverrides, isListContent, null);
    }

    public static void mergeRequestOverrides(PropertyList pagedata, PropertyList listElement, PropertyList toolbarElement, PropertyList searchbarElement, PropertyList layout, PropertyList requestOverrides, boolean isListContent, PageContext pageContext) {
        block146: {
            PropertyList vs_ovv;
            boolean isPagingRequest;
            block147: {
                PropertyList query;
                PropertyList s_temp;
                PropertyListCollection sequence;
                String temp;
                PropertyList t_layout;
                String sdcid;
                if (requestOverrides == null) break block146;
                isPagingRequest = false;
                if (isListContent && pagedata.getProperty("pageno").length() > 0) {
                    isPagingRequest = true;
                }
                if ((sdcid = requestOverrides.getProperty("sdcid", "")).length() > 0) {
                    if (searchbarElement != null) {
                        searchbarElement.setId("advancedsearch");
                        searchbarElement.setProperty("sdcid", sdcid);
                    }
                    if (toolbarElement != null) {
                        toolbarElement.setId("advancedtoolbar");
                        toolbarElement.setProperty("sdcid", sdcid);
                    }
                    if (listElement != null) {
                        listElement.setId("list");
                        listElement.setProperty("sdcid", sdcid);
                    }
                }
                if (isListContent) break block147;
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
                if ((temp = requestOverrides.getProperty("lookupcallback", "")).length() > 0) {
                    pagedata.setProperty("lookupcallback", temp);
                }
                if (toolbarElement != null) {
                    PropertyListCollection t_buttons_ovv;
                    if (requestOverrides.containsKey("hidetoolbar") && requestOverrides.getProperty("hidetoolbar", "N").equalsIgnoreCase("Y")) {
                        toolbarElement.setProperty("displaystyle", "None");
                    }
                    if ((t_buttons_ovv = requestOverrides.getCollection("buttons")) != null) {
                        PropertyListCollection t_buttons = toolbarElement.getCollection("buttons");
                        if (t_buttons == null) {
                            t_buttons = new PropertyListCollection();
                            toolbarElement.setProperty("buttons", t_buttons);
                        }
                        for (int t_i = 0; t_i < t_buttons_ovv.size(); ++t_i) {
                            PropertyList t_btn_up_ovv;
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
                            if (t_btn_sp_ovv != null) {
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
                                if ((temp = t_btn_sp_ovv.getProperty("target", "")).length() > 0) {
                                    t_btn_sp.setProperty("target", temp);
                                }
                                if ((temp = t_btn_sp_ovv.getProperty("storeselection", "")).length() > 0) {
                                    t_btn_sp.setProperty("storeselection", temp);
                                }
                            }
                            if ((t_btn_up_ovv = t_btn_ovv.getPropertyList("userbuttonprops")) == null) continue;
                            t_btn.setProperty("buttontype", "User");
                            PropertyList t_btn_up = t_btn.getPropertyList("userbuttonprops");
                            if (t_btn_up == null) {
                                t_btn_up = new PropertyList();
                                t_btn_up.setId("userbuttonprops");
                                t_btn.setProperty("userbuttonprops", t_btn_up);
                            }
                            if ((temp = t_btn_up_ovv.getProperty("action", "")).length() <= 0) continue;
                            t_btn_up.setProperty("action", temp);
                        }
                    }
                }
                if (searchbarElement == null) break block146;
                PropertyList t_search_ovv = requestOverrides.getPropertyList("advancedsearch");
                if (t_search_ovv != null) {
                    temp = t_search_ovv.getProperty("sdcid", sdcid);
                    if (temp.length() > 0) {
                        searchbarElement.setProperty("sdcid", temp);
                    }
                    if ((temp = t_search_ovv.getProperty("show", "")).length() > 0) {
                        searchbarElement.setProperty("show", temp);
                    }
                    if ((temp = t_search_ovv.getProperty("showinitially", "")).length() > 0) {
                        searchbarElement.setProperty("showinitially", temp);
                    }
                    if ((temp = t_search_ovv.getProperty("showtopsearch", "")).length() > 0) {
                        searchbarElement.setProperty("showtopsearch", temp);
                    }
                    if ((temp = t_search_ovv.getProperty("forcetopsearchonly", "")).length() > 0) {
                        searchbarElement.setProperty("forcetopsearchonly", temp);
                    }
                }
                if ((sequence = searchbarElement.getCollection("sequence")) == null) {
                    sequence = new PropertyListCollection();
                    searchbarElement.setProperty("sequence", sequence);
                }
                if (sequence.size() == 0) {
                    if (t_search_ovv != null && t_search_ovv.getCollection("sequence") != null && t_search_ovv.getCollection("sequence").size() > 0) {
                        for (int s = 0; s < t_search_ovv.getCollection("sequence").size(); ++s) {
                            sequence.add(t_search_ovv.getCollection("sequence").get(s));
                        }
                    } else {
                        PropertyList basicSearch = searchbarElement.getPropertyList("basicsearch");
                        if (basicSearch == null) {
                            basicSearch = new PropertyList();
                            searchbarElement.setProperty("basicsearch", basicSearch);
                        }
                        searchbarElement.getPropertyList("basicsearch").setProperty("displayoption", "T");
                        s_temp = new PropertyList();
                        s_temp.setProperty("id", "basic1");
                        s_temp.setProperty("contentname", "Basic Search");
                        s_temp.setProperty("show", "Y");
                        s_temp.setProperty("initiallyexpanded", "Y");
                        sequence.add(s_temp);
                        s_temp = new PropertyList();
                        s_temp.setProperty("id", "query1");
                        s_temp.setProperty("contentname", "Queries");
                        s_temp.setProperty("show", "Y");
                        s_temp.setProperty("initiallyexpanded", "Y");
                        sequence.add(s_temp);
                    }
                }
                if ((query = searchbarElement.getPropertyList("querysearch")) == null) {
                    PropertyList t_query_ovv;
                    query = new PropertyList();
                    searchbarElement.setProperty("querysearch", query);
                    query.setProperty("style", "hyperlink");
                    query.setProperty("displayoption", "T");
                    PropertyList propertyList = t_query_ovv = t_search_ovv != null ? t_search_ovv.getPropertyList("querysearch") : null;
                    if (t_query_ovv != null && (temp = t_query_ovv.getProperty("category", "")).length() > 0) {
                        query.setProperty("category", temp);
                    }
                }
                if (searchbarElement.getProperty("forcetopsearchonly", "").equalsIgnoreCase("Y")) {
                    PropertyList basic = searchbarElement.getPropertyList("basicsearch");
                    if (basic == null) {
                        basic = new PropertyList();
                        searchbarElement.setProperty("basicsearch", basic);
                    }
                    basic.setProperty("displayoption", "T");
                    PropertyList querysearch = searchbarElement.getPropertyList("querysearch");
                    if (querysearch == null) {
                        querysearch = new PropertyList();
                        searchbarElement.setProperty("querysearch", querysearch);
                    }
                    querysearch.setProperty("displayoption", "T");
                }
                if ((temp = requestOverrides.getProperty("defaultquery", "")).length() <= 0) break block146;
                query.setProperty("default", temp);
                if (sequence.find("contentname", "Queries") != null) break block146;
                s_temp = new PropertyList();
                s_temp.setProperty("id", "query2");
                s_temp.setProperty("contentname", "Queries");
                s_temp.setProperty("show", "Y");
                s_temp.setProperty("initiallyexpanded", "Y");
                sequence.add(s_temp);
                break block146;
            }
            String temp = requestOverrides.getProperty("includetemplates", "");
            if (temp.length() > 0) {
                pagedata.setProperty("includetemplates", temp);
            }
            if ((temp = EncryptDecrypt.unobfsql(requestOverrides.getProperty("restrictivewhere", ""))).length() > 0) {
                String mw = pagedata.getProperty("mergewhere", "Merge");
                String curr = EncryptDecrypt.unobfsql(pagedata.getProperty("restrictivewhere"));
                if (mw.equalsIgnoreCase("override")) {
                    pagedata.setProperty("restrictivewhere", EncryptDecrypt.obfsql(temp));
                } else if (!mw.equalsIgnoreCase("ignore") && mw.equalsIgnoreCase("merge")) {
                    if (curr.length() > 0) {
                        pagedata.setProperty("restrictivewhere", EncryptDecrypt.obfsql("( " + temp + ") AND ( " + curr + " )"));
                    } else {
                        pagedata.setProperty("restrictivewhere", EncryptDecrypt.obfsql(temp));
                    }
                }
            }
            if (!isPagingRequest && (temp = requestOverrides.getProperty("queryfrom", "")).length() > 0) {
                pagedata.setProperty("queryfrom", temp);
            }
            if (!isPagingRequest && (temp = requestOverrides.getProperty("querywhere", "")).length() > 0) {
                pagedata.setProperty("querywhere", temp);
            }
            if ((vs_ovv = requestOverrides.getPropertyList("versionstatus")) != null) {
                PropertyList vs_org = pagedata.getPropertyList("versionstatus");
                if (vs_org == null) {
                    vs_org = new PropertyList();
                }
                if ((temp = vs_ovv.getProperty("provisional", "")).length() > 0) {
                    vs_org.setProperty("provisional", temp);
                }
                if ((temp = vs_ovv.getProperty("current", "")).length() > 0) {
                    vs_org.setProperty("current", temp);
                }
                if ((temp = vs_ovv.getProperty("expired", "")).length() > 0) {
                    vs_org.setProperty("expired", temp);
                }
                if ((temp = vs_ovv.getProperty("active", "")).length() > 0) {
                    vs_org.setProperty("active", temp);
                }
                pagedata.setProperty("versionstatus", vs_org);
            }
            if (listElement != null) {
                PropertyListCollection t_columns_ovv;
                PropertyListCollection t_columns;
                PropertyListCollection t_sorts_ovv;
                PropertyListCollection t_sorts;
                PropertyListCollection t_groups_ovv;
                PropertyListCollection t_groups;
                temp = requestOverrides.getProperty("listid", "");
                if (temp.length() > 0) {
                    listElement.setProperty("listid", temp);
                }
                if ((temp = requestOverrides.getProperty("rowsperpage", "")).length() > 0) {
                    listElement.setProperty("rowsperpage", temp);
                }
                if ((temp = requestOverrides.getProperty("selectortype", "")).length() > 0) {
                    listElement.setProperty("selectortype", temp);
                }
                if ((temp = requestOverrides.getProperty("selectiontype", "")).length() > 0) {
                    listElement.setProperty("selectiontype", temp);
                }
                if ((temp = requestOverrides.getProperty("timezoneoffset")).length() > 0) {
                    listElement.setProperty("timezoneoffset", temp);
                }
                if ((t_groups = listElement.getCollection("groupby")) == null) {
                    t_groups = new PropertyListCollection();
                    listElement.setProperty("groupby", t_groups);
                }
                if ((t_groups_ovv = requestOverrides.getCollection("groupby")) != null) {
                    Object sdc = null;
                    for (int t_i = 0; t_i < t_groups_ovv.size(); ++t_i) {
                        PropertyList t_group_ovv = t_groups_ovv.getPropertyList(t_i);
                        PropertyList t_group = null;
                        String t_id = t_group_ovv.getProperty("id", "");
                        if (t_id.length() > 0 && (t_group = t_groups.getPropertyList(t_id)) == null) {
                            t_group = t_groups.find("id", t_id);
                        }
                        String t_colid = t_group_ovv.getProperty("columnid", "");
                        if (t_group == null && t_colid.length() > 0) {
                            t_group = t_groups.find("columnid", t_colid);
                        }
                        if (t_group != null) {
                            PropertyList t_link_ovv;
                            if (t_id.length() > 0) {
                                t_group.setProperty("id", t_id);
                            }
                            if (t_colid.length() > 0) {
                                t_group.setProperty("columnid", t_colid);
                            }
                            if ((temp = t_group_ovv.getProperty("title", "")).length() > 0) {
                                t_group.setProperty("title", temp);
                            }
                            if ((temp = t_group_ovv.getProperty("displayvalue", "")).length() > 0) {
                                t_group.setProperty("displayvalue", temp);
                            }
                            if ((temp = t_group_ovv.getProperty("useasfilter", "")).length() > 0) {
                                t_group.setProperty("useasfilter", temp);
                            }
                            if (t_group_ovv.containsKey("editorstyleid")) {
                                if (t_group_ovv.get("editorstyleid") instanceof PropertyList) {
                                    t_group.setProperty("editorstyleid", t_group_ovv.getPropertyList("editorstyleid"));
                                } else {
                                    temp = t_group_ovv.getProperty("editorstyleid", "");
                                    if (temp.length() > 0) {
                                        t_group.setProperty("editorstyleid", temp);
                                    }
                                }
                            }
                            if ((temp = t_group_ovv.getProperty("translatevalue", "")).length() > 0) {
                                t_group.setProperty("translatevalue", temp);
                            }
                            if ((t_link_ovv = t_group_ovv.getPropertyList("link")) == null) continue;
                            if (t_link_ovv.getProperty("show").equalsIgnoreCase("N")) {
                                if (t_group.getPropertyList("link") == null) continue;
                                t_group.remove("link");
                                continue;
                            }
                            PropertyList t_link = t_group.getPropertyList("link");
                            if (t_link == null) {
                                t_link = new PropertyList();
                            }
                            if ((temp = t_link_ovv.getProperty("href", "")).length() > 0) {
                                t_link.setProperty("href", temp);
                            }
                            if ((temp = t_link_ovv.getProperty("target", "")).length() > 0) {
                                t_link.setProperty("target", temp);
                            }
                            if ((temp = t_link_ovv.getProperty("tip", "")).length() > 0) {
                                t_link.setProperty("tip", temp);
                            }
                            if ((temp = t_link_ovv.getProperty("sapphiredialog", "")).length() > 0) {
                                t_link.setProperty("sapphiredialog", temp);
                            }
                            t_group.setProperty("link", t_link);
                            continue;
                        }
                        t_group = (PropertyList)t_group_ovv.clone();
                        t_groups.add(t_group);
                    }
                }
                if ((t_sorts = listElement.getCollection("sortby")) == null) {
                    t_sorts = new PropertyListCollection();
                    listElement.setProperty("sortby", t_sorts);
                }
                if ((t_sorts_ovv = requestOverrides.getCollection("sortby")) != null) {
                    Object sdc = null;
                    for (int t_i = 0; t_i < t_sorts_ovv.size(); ++t_i) {
                        PropertyList t_sort_ovv = t_sorts_ovv.getPropertyList(t_i);
                        PropertyList t_sort = null;
                        String t_id = t_sort_ovv.getProperty("id", "");
                        if (t_id.length() > 0 && (t_sort = t_sorts.getPropertyList(t_id)) == null) {
                            t_sort = t_sorts.find("id", t_id);
                        }
                        String t_colid = t_sort_ovv.getProperty("columnid", "");
                        if (t_sort == null && t_colid.length() > 0) {
                            t_sort = t_sorts.find("columnid", t_colid);
                        }
                        if (t_sort != null) {
                            if (t_id.length() > 0) {
                                t_sort.setProperty("id", t_id);
                            }
                            if (t_colid.length() > 0) {
                                t_sort.setProperty("columnid", t_colid);
                            }
                            if ((temp = t_sort_ovv.getProperty("asc_desc", "")).length() > 0) {
                                t_sort.setProperty("asc_desc", temp);
                            }
                            if ((temp = t_sort_ovv.getProperty("callback", "")).length() <= 0) continue;
                            t_sort.setProperty("callback", temp);
                            continue;
                        }
                        t_sort = (PropertyList)t_sort_ovv.clone();
                        t_sorts.add(t_sort);
                    }
                }
                if ((t_columns = listElement.getCollection("columns")) == null) {
                    t_columns = new PropertyListCollection();
                    listElement.setProperty("columns", t_columns);
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
                        if (t_col == null && t_colid.length() > 0 && (t_col = t_columns.find("columnid", t_colid)) == null && (sdc != null || listElement != null && pageContext != null && listElement.getProperty("sdcid", "").length() > 0)) {
                            if (sdc == null) {
                                sdc = new SDCProcessor(pageContext).getPropertyList(listElement.getProperty("sdcid", ""));
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
                            PropertyList t_link_ovv;
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
                                if ("Display Only".equals(temp) || "Display and Return".equals(temp)) {
                                    t_col.setProperty("mode", "Display Text");
                                    if ("Display and Return".equals(temp)) {
                                        t_col.setProperty("returnvalue", "Y");
                                    }
                                } else {
                                    t_col.setProperty("mode", temp);
                                }
                            }
                            if ((temp = t_col_ovv.getProperty("width", "")).length() > 0) {
                                t_col.setProperty("width", temp);
                            }
                            if ((temp = t_col_ovv.getProperty("align", "")).length() > 0) {
                                t_col.setProperty("align", temp);
                            }
                            if ((temp = t_col_ovv.getProperty("displayvalue", "")).length() > 0) {
                                t_col.setProperty("displayvalue", temp);
                            }
                            if ((temp = t_col_ovv.getProperty("pseudocolumn", "")).length() > 0) {
                                t_col.setProperty("pseudocolumn", temp);
                            }
                            if ((temp = t_col_ovv.getProperty("format", "")).length() > 0) {
                                t_col.setProperty("format", temp);
                            }
                            if ((temp = t_col_ovv.getProperty("translatevalue", "")).length() > 0) {
                                t_col.setProperty("translatevalue", temp);
                            }
                            if ((t_link_ovv = t_col_ovv.getPropertyList("link")) == null) continue;
                            if (t_link_ovv.getProperty("show").equalsIgnoreCase("N")) {
                                if (t_col.getPropertyList("link") == null) continue;
                                t_col.remove("link");
                                continue;
                            }
                            PropertyList t_link = t_col.getPropertyList("link");
                            if (t_link == null) {
                                t_link = new PropertyList();
                            }
                            if ((temp = t_link_ovv.getProperty("href", "")).length() > 0) {
                                t_link.setProperty("href", temp);
                            }
                            if ((temp = t_link_ovv.getProperty("target", "")).length() > 0) {
                                t_link.setProperty("target", temp);
                            }
                            if ((temp = t_link_ovv.getProperty("tip", "")).length() > 0) {
                                t_link.setProperty("tip", temp);
                            }
                            if ((temp = t_link_ovv.getProperty("sapphiredialog", "")).length() > 0) {
                                t_link.setProperty("sapphiredialog", temp);
                            }
                            t_col.setProperty("link", t_link);
                            continue;
                        }
                        t_col = (PropertyList)t_col_ovv.clone();
                        t_columns.add(t_col);
                    }
                }
            }
        }
    }

    public static enum ListMode {
        SELECTABLE("selectable", "rc?command=image&image=FlatBlackBulletList2"),
        LIST("list", "rc?command=image&image=FlatBlackBulletList2"),
        MAP("map", "rc?command=image&image=FlatBlackMapGps"),
        CALENDAR("calendar", "rc?command=image&image=FlatBlackCalendar");

        private final String mode;
        private final String image;

        private ListMode(String mode, String image) {
            this.mode = mode;
            this.image = image;
        }

        public static ListMode fromString(String mode) {
            for (ListMode listMode : ListMode.values()) {
                if (!listMode.mode.equalsIgnoreCase(mode)) continue;
                return listMode;
            }
            return LIST;
        }

        public String toString() {
            return this.mode.toLowerCase();
        }

        public String getImage() {
            return this.image;
        }
    }
}

