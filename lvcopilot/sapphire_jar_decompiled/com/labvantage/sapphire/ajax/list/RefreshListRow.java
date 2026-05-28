/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.ajax.list;

import com.labvantage.sapphire.pageelements.list.CalendarHelper;
import com.labvantage.sapphire.pageelements.list.List;
import com.labvantage.sapphire.pageelements.list.ListRow;
import com.labvantage.sapphire.pageelements.list.MapHelper;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.util.groovy.GroovyPolicyUtil;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.tagext.SDITagInfo;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class RefreshListRow
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 84852 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = null;
        ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid());
        try {
            String extraColumns;
            ajaxResponse = new AjaxResponse(request, response);
            String sdcid = ajaxResponse.getRequestParameter("sdcid");
            String keyid1 = ajaxResponse.getRequestParameter("keyid1");
            String keyid2 = ajaxResponse.getRequestParameter("keyid2");
            String keyid3 = ajaxResponse.getRequestParameter("keyid3");
            String pageid = ajaxResponse.getRequestParameter("pageid");
            String group = ajaxResponse.getRequestParameter("group");
            String isScanFind = ajaxResponse.getRequestParameter("isScanFind");
            List.ListMode listMode = List.ListMode.fromString(ajaxResponse.getRequestParameter("listmode", "list").toLowerCase());
            JSONObject map = new JSONObject(ajaxResponse.getRequestParameter("map"));
            if (ajaxResponse.getRequestParameter("callbck").length() > 0) {
                ajaxResponse.setCallback(ajaxResponse.getRequestParameter("callbck"));
            }
            String[] keyCols = new String[3];
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            PageContext pageContext = ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
            RequestProcessor rp = new RequestProcessor(pageContext);
            RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
            PropertyList element = rp.getWebPageProperties(pageid, requestContext);
            PropertyListCollection columns = element.getPropertyList("list").getCollection("columns");
            ArrayList linkcolist = new ArrayList();
            PropertyList sdc = (PropertyList)pageContext.getAttribute(sdcid + "_props");
            if (sdcid.length() > 0 && sdc == null) {
                sdc = sdcProcessor.getPropertyList(sdcid);
                pageContext.setAttribute(sdcid + "_props", (Object)sdc);
                String keycolid1 = sdc.getProperty("keycolid1");
                String keycolid2 = sdc.getProperty("keycolid2");
                String keycolid3 = sdc.getProperty("keycolid3");
                String desccol = sdc.getProperty("desccol");
                keyCols[0] = keycolid1;
                keyCols[1] = keycolid2;
                keyCols[2] = keycolid3;
                if (columns != null && columns.size() > 0) {
                    for (int i = 0; i < columns.size(); ++i) {
                        PropertyList column = columns.getPropertyList(i);
                        String columnid = column.getProperty("columnid");
                        if (columnid.equals("keycolid1")) {
                            column.setProperty("columnid", keycolid1);
                            continue;
                        }
                        if (columnid.equals("keycolid2")) {
                            if (keycolid2 == null || keycolid2.length() <= 0) continue;
                            column.setProperty("columnid", keycolid2);
                            continue;
                        }
                        if (columnid.equals("keycolid3")) {
                            if (keycolid3 == null || keycolid3.length() <= 0) continue;
                            column.setProperty("columnid", keycolid3);
                            continue;
                        }
                        if (!columnid.equals("desccolid") && !columnid.equals("desccol") || desccol == null || desccol.length() <= 0) continue;
                        column.setProperty("columnid", desccol);
                    }
                }
            }
            if (listMode.equals((Object)List.ListMode.MAP)) {
                MapHelper helper = new MapHelper(pageContext);
                helper.checkCoordinateColumns(sdcid, columns, false);
            }
            if (listMode.equals((Object)List.ListMode.CALENDAR)) {
                CalendarHelper calendarHelper = new CalendarHelper(pageContext);
                calendarHelper.forceCalendarColumns(element.getPropertyList("list"), columns);
            }
            this.getRequestItemCols(columns, linkcolist);
            String _request = linkcolist.size() > 0 ? "primary[" + this.getColumnList(linkcolist) + "]" : "primary";
            if (_request != null && _request.indexOf("[currentuser]") >= 0) {
                _request = StringUtil.replaceAll(_request, "[currentuser]", requestContext.getProperty("sysuserid"));
            }
            if (_request != null && _request.indexOf("[sdcid]") >= 0) {
                _request = StringUtil.replaceAll(_request, "[sdcid]", sdcid);
            }
            if (_request != null && (_request.indexOf("[keycolid1]") >= 0 || _request.indexOf("[primarytable]") >= 0) && pageContext != null) {
                _request = StringUtil.replaceAll(_request, "[keycolid1]", sdcProcessor.getProperty(sdcid, "keycolid1"));
                _request = StringUtil.replaceAll(_request, "[keycolid2]", sdcProcessor.getProperty(sdcid, "keycolid2"));
                _request = StringUtil.replaceAll(_request, "[keycolid3]", sdcProcessor.getProperty(sdcid, "keycolid3"));
                _request = StringUtil.replaceAll(_request, "[primarytable]", sdcProcessor.getProperty(sdcid, "tableid"));
            }
            SDIRequest rq = new SDIRequest();
            rq.setSDCid(sdcid);
            rq.setKeyid1List(keyid1);
            rq.setQueryWhere("1=1");
            if (keyid2 != null && keyid2.length() > 0) {
                rq.setKeyid2List(keyid2);
            }
            if (keyid2 != null && keyid2.length() > 0) {
                rq.setKeyid3List(keyid3);
            }
            rq.setRequestItem(_request);
            SDIData sdiData = this.getSDIProcessor().getSDIData(rq);
            DataSet primaryDS = sdiData.getDataset("primary");
            QueryData queryData = new QueryData("primary", primaryDS);
            HashMap<String, QueryData> queryDataMap = new HashMap<String, QueryData>();
            queryDataMap.put("primary", queryData);
            SDITagInfo sdiinfo = new SDITagInfo(queryDataMap);
            sdiinfo.setSdcid(sdcid);
            sdiinfo.setKeycols(keyCols);
            if (group != null && group.trim().length() > 0) {
                queryData.resetGroup("", group);
            }
            PropertyList pagedata = null;
            pagedata = (PropertyList)pageContext.getRequest().getAttribute("pagedata");
            if (pagedata == null) {
                pagedata = new PropertyList();
            }
            for (int i = 0; i < columns.size(); ++i) {
                String mode;
                PropertyList column = columns.getPropertyList(i);
                if (column == null || (mode = column.getProperty("mode", "")).indexOf("$G{") != 0) continue;
                HashMap<String, Object> bindMap = new HashMap<String, Object>();
                bindMap.put("element", element.getPropertyList("list"));
                bindMap.put("pagedata", pagedata);
                bindMap.put("sdc", sdcProcessor.getSDCProperties(sdcid));
                bindMap.put("policy", new GroovyPolicyUtil(pageContext));
                bindMap.put("user", connectionInfo.getUserAttributeMap());
                try {
                    mode = GroovyUtil.getInstance(pageContext).evaluateSecure(mode, bindMap);
                    column.setProperty("mode", mode);
                    continue;
                }
                catch (SapphireException se) {
                    Logger.logError(se.getMessage());
                }
            }
            if (listMode.equals((Object)List.ListMode.MAP) && !(extraColumns = element.getPropertyListNotNull("list").getPropertyListNotNull("mapprops").getProperty("fetchextracolumns")).isEmpty()) {
                MapHelper helper = new MapHelper(pageContext);
                helper.setExtraColumns(sdiinfo, extraColumns, element.getPropertyListNotNull("list"));
            }
            ListRow listRow = new ListRow(pageContext, sdiinfo, sdcProcessor);
            listRow.setDatasetName("primary");
            listRow.setListmode(listMode);
            listRow.setElementProperties(element.getPropertyList("list"));
            String appearance = element.getProperty("appearance");
            StringBuffer html = new StringBuffer();
            JSONArray arrayRows = new JSONArray();
            while (queryData.nextRow(-1)) {
                String keyId1 = queryData.getValue(sdc.getProperty("keycolid1"), "");
                String keyId2 = queryData.getValue(sdc.getProperty("keycolid2"), "");
                String keyId3 = queryData.getValue(sdc.getProperty("keycolid3"), "");
                String combinedKeyid = keyId1;
                String keyid = keyId1;
                combinedKeyid = combinedKeyid + (keyId2.length() > 0 ? "|" + keyId2 : "");
                keyid = keyid + (keyId2.length() > 0 ? ";" + keyId2 : "");
                combinedKeyid = combinedKeyid + (keyId3.length() > 0 ? "|" + keyId3 : "");
                keyid = keyid + (keyId3.length() > 0 ? ";" + keyId3 : "");
                int currentRow = map.has(combinedKeyid) ? map.getInt(combinedKeyid) : 0;
                JSONObject rowObject = new JSONObject();
                if ("true".equals(isScanFind)) {
                    listRow.getHtml();
                    rowObject.put("keyid1", keyid1);
                    rowObject.put("id", listRow.getReturnvalue());
                    rowObject.put("rowvalue", listRow.getRowvalue());
                } else {
                    if (listMode.equals((Object)List.ListMode.LIST)) {
                        html.append("<tr name=\"list_tablerow\" rownum=\"").append(currentRow).append("\" class=\"list_").append(appearance).append("tablerow").append(currentRow % 2 == 0 ? "even" : "odd").append(queryData.isGroupDefined() ? "group" : "").append("\">");
                    }
                    html.append(listRow.getHtml());
                    if (listMode.equals((Object)List.ListMode.LIST)) {
                        html.append("</tr>");
                    }
                    rowObject.put("rownum", currentRow);
                    rowObject.put("data", html.toString());
                    rowObject.put("keyid", keyid);
                }
                arrayRows.put(rowObject);
                html = new StringBuffer();
            }
            JSONObject rows = new JSONObject();
            rows.put("rows", arrayRows);
            ajaxResponse.addCallbackArgument("rows", rows.toString());
            ajaxResponse.print();
        }
        catch (Exception e) {
            ajaxResponse.setErrorCallback("rowRefreshErrorCallback");
            ajaxResponse.addCallbackArgument("msg", "Error fetching data:" + e.getMessage());
            ajaxResponse.print();
        }
    }

    private void getRequestItemCols(PropertyListCollection columns, ArrayList columnlist) {
        if (columns != null && columns.size() > 0) {
            for (int i = 0; i < columns.size(); ++i) {
                String colid;
                if ("Do Not Retrieve".equals(columns.getPropertyList(i).getProperty("mode")) || (colid = columns.getPropertyList(i).getProperty("columnid")).length() <= 0 || columnlist.indexOf(colid) >= 0) continue;
                columnlist.add(colid);
            }
        }
    }

    private String getColumnList(ArrayList columnlist) {
        StringBuffer cols = new StringBuffer();
        for (int i = 0; i < columnlist.size(); ++i) {
            if (i == 0) {
                cols.append((String)columnlist.get(i));
                continue;
            }
            cols.append("," + (String)columnlist.get(i));
        }
        return cols.toString();
    }
}

