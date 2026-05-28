/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.gwt.server;

import com.labvantage.sapphire.util.json.JSONUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DetailInfoRequest
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            SDIData sdidata;
            RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
            String connectionid = requestContext.getConnectionId();
            ConnectionInfo connectionInfo = new ConnectionProcessor(connectionid).getConnectionInfo(connectionid);
            M18NUtil m18NUtil = new M18NUtil(connectionInfo);
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            String type = request.getParameter("detailtype");
            DataSet ds = null;
            SDIProcessor sdiProcessor = new SDIProcessor(requestContext.getConnectionId());
            SDIRequest sdiRequest = new SDIRequest();
            String sdcid = request.getParameter("sdcid");
            String keyid1 = request.getParameter("keyid1");
            String keyid2 = request.getParameter("keyid2");
            String keyid3 = request.getParameter("keyid3");
            String paramlistid = request.getParameter("paramlistid");
            String paramlistversionid = request.getParameter("paramlistversionid");
            String variantid = request.getParameter("variantid");
            String dataset = request.getParameter("dataset");
            String paramid = request.getParameter("paramid");
            String paramtype = request.getParameter("paramtype");
            String replicateid = request.getParameter("replicateid");
            sdiRequest.setSDCid(sdcid);
            sdiRequest.setKeyid1List(keyid1);
            sdiRequest.setKeyid2List(keyid2);
            sdiRequest.setKeyid3List(keyid3);
            sdiRequest.setRetrieve(true);
            String elementJSONString = request.getParameter("element");
            JSONObject element = new JSONObject(elementJSONString);
            JSONArray columns = element.getJSONArray("columns");
            StringBuffer requestCols = new StringBuffer();
            boolean hasExtendedColumn = false;
            for (int i = 0; i < columns.length(); ++i) {
                if (columns.getJSONObject(i).isNull("columnid")) continue;
                String columnid = columns.getJSONObject(i).getString("columnid");
                if (i == 0) {
                    requestCols.append("[" + columnid);
                    continue;
                }
                requestCols.append("," + columnid);
            }
            if (requestCols.length() > 0) {
                requestCols.append("]");
            }
            PropertyList sdcProps = null;
            if ("primary".equals(type)) {
                sdiRequest.setRequestItem("primary" + requestCols.toString());
                sdiRequest.setReturnMaskedData(true);
                sdidata = sdiProcessor.getSDIData(sdiRequest);
                ds = sdidata.getDataset("primary");
                sdcProps = sdcProcessor.getPropertyList(sdidata.getSdcid());
            } else if ("dataset".equals(type) || "dataitem".equals(type)) {
                sdiRequest.setParamlistidList(paramlistid);
                sdiRequest.setParamlistversionidList(paramlistversionid);
                sdiRequest.setVariantidList(variantid);
                sdiRequest.setDatasetList(dataset);
                sdiRequest.setRequestItem(type + requestCols.toString());
                sdiRequest.setPropsMatch(true);
                sdiRequest.setSecurityBypassCode("D".equals(new SDCProcessor(requestContext.getConnectionId()).getProperty("DataSet", "accesscontrolledflag")) ? 2 : 0);
                sdiRequest.setRetrieve(true);
                sdidata = sdiProcessor.getSDIData(sdiRequest);
                ds = sdidata.getDataset(type);
                if ("dataitem".equals(type)) {
                    HashMap<String, Object> filterMap = new HashMap<String, Object>();
                    filterMap.put("paramid", paramid);
                    filterMap.put("paramtype", paramtype);
                    filterMap.put("replicateid", new BigDecimal(replicateid));
                    ds = ds.getFilteredDataSet(filterMap);
                    sdcProps = sdcProcessor.getPropertyList("DataItem");
                } else {
                    sdcProps = sdcProcessor.getPropertyList("DataSet");
                }
            }
            PropertyListCollection sdccolumns = sdcProps.getCollection("columns");
            for (int i = 0; i < sdccolumns.size(); ++i) {
                PropertyList sdccolumnPL = sdccolumns.getPropertyList(i);
                if (sdccolumnPL == null) continue;
                String columnid = sdccolumnPL.getProperty("columnid");
                if (!"Y".equals(sdccolumnPL.getProperty("timezoneindependent")) || !ds.isValidColumn(columnid)) continue;
                ds.setDateDisplayFormat(columnid, m18NUtil.getDefaultDateOnlyFormat(false));
            }
            response.getWriter().write(JSONUtil.toJSONObject(ds).toString());
        }
        catch (Exception e) {
            throw new ServletException((Throwable)e);
        }
    }
}

