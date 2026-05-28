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

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.gwt.server.DataItemCrossTabModel;
import com.labvantage.sapphire.pageelements.gwt.server.GWTDataEntry;
import com.labvantage.sapphire.servlet.RequestProcessor;
import java.util.Iterator;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LiveDataEntryCheckRequest
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        this.handleLiveLimitCheckRequest(request, response);
    }

    private void handleLiveLimitCheckRequest(HttpServletRequest request, HttpServletResponse response) {
        try {
            RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
            String calculatemodifiedtestsonly = requestContext.getProperty("calculatemodifiedtestsonly");
            String calculatemodifieddatasetsonly = requestContext.getProperty("calculatemodifieddatasetsonly");
            String hascalcexcludechanges = requestContext.getProperty("hascalcexcludechanges");
            String jsonString = request.getParameter("jsonsaveobject");
            JSONObject saveObject = new JSONObject(jsonString);
            JSONObject columnIndexMap = saveObject.getJSONObject("columns");
            JSONObject rowStatusMap = saveObject.getJSONObject("rowStatusMap");
            JSONObject rowValueMap = saveObject.getJSONObject("rowValueMap");
            String sdcid = saveObject.getString("sdcid") == null ? "Sample" : saveObject.getString("sdcid");
            String redosdcid = request.getParameter("redosdcid");
            String redokeyid1 = request.getParameter("redokeyid1");
            String redokeyid2 = request.getParameter("redokeyid2");
            String redokeyid3 = request.getParameter("redokeyid3");
            int[] diKeycolindexes = new int[]{columnIndexMap.getInt("keyid1"), columnIndexMap.getInt("keyid2"), columnIndexMap.getInt("keyid3"), columnIndexMap.getInt("paramlistid"), columnIndexMap.getInt("paramlistversionid"), columnIndexMap.getInt("variantid"), columnIndexMap.getInt("dataset"), columnIndexMap.getInt("paramid"), columnIndexMap.getInt("paramtype"), columnIndexMap.getInt("replicateid")};
            int enterdataitempropCount = diKeycolindexes.length + 2;
            int[] columnIndexes = new int[enterdataitempropCount];
            System.arraycopy(diKeycolindexes, 0, columnIndexes, 0, diKeycolindexes.length);
            columnIndexes[10] = columnIndexMap.getInt("enteredtext");
            columnIndexes[11] = columnIndexMap.getInt("calcexcludeflag");
            String[] enterdataitempropids = new String[enterdataitempropCount];
            String[] diKeycolids = new SDIData().getKeys("dataitem");
            System.arraycopy(diKeycolids, 1, enterdataitempropids, 0, diKeycolids.length - 1);
            enterdataitempropids[10] = "enteredtext";
            enterdataitempropids[11] = "calcexcludeflag";
            StringBuilder[] enterdataitempropvalues = new StringBuilder[enterdataitempropCount];
            PropertyList enterdataitemprops = new PropertyList();
            enterdataitemprops.setProperty("sdcid", sdcid);
            enterdataitemprops.put("dataentrypage_allsdis", saveObject.optString("dataentrypage_allsdis"));
            Iterator itr = rowValueMap.keys();
            boolean hasDataEntry = false;
            while (itr.hasNext()) {
                String key = (String)itr.next();
                String rowStatus = rowStatusMap.getString(key);
                if (!"B".equals(rowStatus) && !"D".equals(rowStatus)) continue;
                JSONArray currentrow = rowValueMap.getJSONArray(key);
                for (int i = 0; i < enterdataitempropids.length; ++i) {
                    String value;
                    if (enterdataitempropvalues[i] == null) {
                        enterdataitempropvalues[i] = new StringBuilder();
                    }
                    value = (value = currentrow.getString(columnIndexes[i])).contains(";") ? StringUtil.replaceAll(value, ";", "#semicolon#") : value;
                    enterdataitempropvalues[i].append(";" + value);
                }
            }
            if (enterdataitempropvalues[0] != null) {
                hasDataEntry = true;
                for (int i = 0; i < enterdataitempropids.length; ++i) {
                    enterdataitemprops.setProperty(enterdataitempropids[i], enterdataitempropvalues[i].substring(1));
                }
            }
            if (!"true".equals(hascalcexcludechanges)) {
                enterdataitemprops.setProperty("calcexcludeflag", "");
            }
            if (hasDataEntry) {
                String secondarysdcJSONString = request.getParameter("secondarysdc");
                if (secondarysdcJSONString != null) {
                    enterdataitemprops.setProperty("redosdcid", redosdcid);
                    enterdataitemprops.setProperty("redokeyid1", redokeyid1);
                    enterdataitemprops.setProperty("redokeyid2", redokeyid2);
                    enterdataitemprops.setProperty("redokeyid3", redokeyid3);
                }
                enterdataitemprops.setProperty("islivelimitchecking", "Y");
                String livelimitcheckmode = requestContext.getProperty("livelimitcheckmode");
                if (livelimitcheckmode.length() > 0) {
                    enterdataitemprops.setProperty("livelimitcheckmode", livelimitcheckmode);
                }
                if (calculatemodifieddatasetsonly.length() > 0) {
                    enterdataitemprops.setProperty("calculatemodifieddatasetsonly", calculatemodifieddatasetsonly);
                }
                if (calculatemodifiedtestsonly.length() > 0) {
                    enterdataitemprops.setProperty("calculatemodifiedtestsonly", calculatemodifiedtestsonly);
                }
                this.getActionProcessor().processAction("EnterDataItem", "1", enterdataitemprops);
                DataSet sdidataitem = (DataSet)enterdataitemprops.get("crosssdi_all_modifieddataitems");
                DataSet sdidataitemspec = (DataSet)enterdataitemprops.get("sdidataitemspec");
                DataSet sdidata = (DataSet)enterdataitemprops.get("sdidata");
                DataSet primary = (DataSet)enterdataitemprops.get("primary");
                String columnheader = request.getParameter("columnheader");
                String rowheader = request.getParameter("rowheader");
                if (secondarysdcJSONString != null && secondarysdcJSONString.length() > 0) {
                    JSONObject secondarySDCObject = new JSONObject(secondarysdcJSONString);
                    PropertyList secondarySDCPL = new PropertyList(secondarySDCObject);
                    String webpageid = request.getParameter("webpageid");
                    String elementid = request.getParameter("elementid");
                    RequestProcessor requestProcessor = new RequestProcessor(requestContext.getConnectionId());
                    PropertyList pagedata = requestProcessor.getWebPageProperties(webpageid, requestContext);
                    PropertyList element = pagedata.getPropertyList(elementid);
                    GWTDataEntry.appendSecondarySDCDataSets(false, element, secondarySDCPL, primary, sdidata, sdidataitem, sdidataitemspec, this.getSDIProcessor(), this.getTranslationProcessor());
                }
                DataItemCrossTabModel model = new DataItemCrossTabModel(requestContext.getConnectionId());
                JSONObject jsonResponseObj = model.toJSONObjectCrossTabModel(sdidataitem, sdidata, primary, sdidataitemspec, StringUtil.split(columnheader, ";"), StringUtil.split(rowheader, ";"));
                jsonResponseObj.put("sdcid", sdcid);
                jsonResponseObj.write(response.getWriter());
            }
        }
        catch (Exception e) {
            Trace.logError("Live limit check error", e);
            this.write("Failed to check data. " + e.getMessage());
        }
    }
}

