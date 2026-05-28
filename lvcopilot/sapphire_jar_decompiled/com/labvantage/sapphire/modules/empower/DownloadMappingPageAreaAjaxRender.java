/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.ServletResponse
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.JspFactory
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.empower;

import com.labvantage.sapphire.modules.empower.DownloadMappingPage;
import com.labvantage.sapphire.modules.empower.DownloadMappingPageArea;
import com.labvantage.sapphire.modules.empower.EmpowerPolicyDef;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.util.PseudoPageContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class DownloadMappingPageAreaAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 78066 $";
    private EmpowerPolicyDef policyDef;
    private ConnectionInfo connectionInfo;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "DownloadMappingPageHandler");
        this.connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid());
        DownloadMappingPageArea.PageArea pageArea = DownloadMappingPageArea.PageArea.valueOf(ajaxResponse.getRequestParameter("pagearea", DownloadMappingPageArea.PageArea.QCBatch.toString()));
        this.logger.debug("DownloadMappingPageAreaAjaxRender pageArea = " + (Object)((Object)pageArea));
        String titles = ajaxResponse.getRequestParameter("titles", "");
        this.policyDef = (EmpowerPolicyDef)request.getSession().getAttribute("__EmpowerPolicyDef");
        if (this.policyDef == null) {
            ajaxResponse.setError(this.getTranslationProcessor().translate("Cannot access Empower Policy."));
            return;
        }
        JSONObject jobTitles = null;
        if (titles.length() > -1) {
            try {
                jobTitles = new JSONObject(titles);
            }
            catch (Exception e) {
                jobTitles = null;
            }
        }
        PropertyList samplesetmethod = null;
        HashMap<String, DataSet> datamap = DownloadMappingPage.getCachedDataMap(request.getSession());
        HashMap<String, DataSet> componentsmap = DownloadMappingPage.getCachedComponentMap(request.getSession());
        HashMap<String, int[]> selectedMap = new HashMap<String, int[]>();
        try {
            JSONObject job = new JSONObject(ajaxResponse.getRequestParameter("selected", ""));
            Iterator it = job.keys();
            while (it.hasNext()) {
                DownloadMappingPageArea.PageArea currentArea = DownloadMappingPageArea.PageArea.valueOf(it.next().toString());
                JSONArray ja = new JSONArray(job.getString(currentArea.toString()));
                int[] selectedItems = new int[ja.length()];
                for (int i = 0; i < ja.length(); ++i) {
                    selectedItems[i] = ja.getInt(i);
                }
                selectedMap.put(currentArea.toString(), selectedItems);
            }
        }
        catch (Exception e) {
            selectedMap = null;
        }
        if (datamap != null && componentsmap != null) {
            if (this.getServlet() != null) {
                int p;
                DataSet componentdata = componentsmap.get(pageArea.toString());
                try {
                    Configuration config = Configuration.getInstance();
                    p = config.getPlatform();
                }
                catch (Exception e) {
                    this.logger.error("Failed to obtain plaform defaulting to JBOSS", e);
                    p = 4;
                }
                PseudoPageContext pageContext = p == 3 ? new PseudoPageContext(servletContext, request, response) : JspFactory.getDefaultFactory().getPageContext((Servlet)this.getServlet(), (ServletRequest)request, (ServletResponse)response, "", true, 0, false);
                try {
                    String mode = ajaxResponse.getRequestParameter("mode", "view");
                    boolean clearSelection = false;
                    if (mode.equalsIgnoreCase("map") || mode.equalsIgnoreCase("mapall") || mode.equalsIgnoreCase("unmap") || mode.equalsIgnoreCase("unmapall")) {
                        String error;
                        String error2;
                        DataSet saveCopyFromData;
                        DataSet saveCopyToData;
                        Object toData;
                        DataSet fromData;
                        ArrayList<DownloadMappingPageArea.PageArea> reloadList = new ArrayList<DownloadMappingPageArea.PageArea>();
                        if (mode.equalsIgnoreCase("map")) {
                            if (datamap.containsKey(pageArea.toString()) && datamap.containsKey(DownloadMappingPageArea.PageArea.SampleSetMethod.toString())) {
                                fromData = datamap.get(pageArea.toString());
                                toData = datamap.get(DownloadMappingPageArea.PageArea.SampleSetMethod.toString());
                                saveCopyToData = ((DataSet)toData).copy();
                                saveCopyFromData = fromData.copy();
                                error2 = DownloadMappingPage.doMap(this.getTranslationProcessor(), fromData, (DataSet)toData, (int[])selectedMap.get(pageArea.toString()), (int[])selectedMap.get(DownloadMappingPageArea.PageArea.SampleSetMethod.toString()), pageArea, this.policyDef);
                                if (error2.length() == 0) {
                                    clearSelection = true;
                                    reloadList.add(pageArea);
                                    reloadList.add(DownloadMappingPageArea.PageArea.SampleSetMethod);
                                } else {
                                    datamap.put(pageArea.toString(), saveCopyFromData);
                                    datamap.put(DownloadMappingPageArea.PageArea.SampleSetMethod.toString(), saveCopyToData);
                                    ajaxResponse.setError(this.getTranslationProcessor().translate(error2));
                                }
                            } else {
                                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not find data for map."));
                            }
                        } else if (mode.equalsIgnoreCase("mapall")) {
                            if (datamap.containsKey(pageArea.toString()) && datamap.containsKey(DownloadMappingPageArea.PageArea.SampleSetMethod.toString())) {
                                fromData = datamap.get(pageArea.toString());
                                toData = datamap.get(DownloadMappingPageArea.PageArea.SampleSetMethod.toString());
                                saveCopyToData = ((DataSet)toData).copy();
                                saveCopyFromData = fromData.copy();
                                error2 = DownloadMappingPage.doMapAll(fromData, (DataSet)toData, pageArea, this.policyDef);
                                if (error2.length() == 0) {
                                    clearSelection = true;
                                    reloadList.add(pageArea);
                                    reloadList.add(DownloadMappingPageArea.PageArea.SampleSetMethod);
                                } else {
                                    datamap.put(pageArea.toString(), saveCopyFromData);
                                    datamap.put(DownloadMappingPageArea.PageArea.SampleSetMethod.toString(), saveCopyToData);
                                    ajaxResponse.setError(error2);
                                }
                                int[] selectedItems = new int[datamap.get(pageArea.toString()).getRowCount()];
                                for (int i = 0; i < selectedItems.length; ++i) {
                                    selectedItems[i] = 1;
                                }
                                selectedMap.put(DownloadMappingPageArea.PageArea.SampleSetMethod.toString(), selectedItems);
                            } else {
                                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not find data for map."));
                            }
                        } else if (mode.equalsIgnoreCase("unmap")) {
                            if (datamap.containsKey(pageArea.toString()) && datamap.containsKey(DownloadMappingPageArea.PageArea.SampleSetMethod.toString())) {
                                error = DownloadMappingPage.doUnmap(this.getTranslationProcessor(), datamap, datamap.get(DownloadMappingPageArea.PageArea.SampleSetMethod.toString()), (int[])selectedMap.get(pageArea.toString()), (int[])selectedMap.get(DownloadMappingPageArea.PageArea.SampleSetMethod.toString()), reloadList, this.policyDef);
                                if (error.length() == 0) {
                                    clearSelection = true;
                                } else {
                                    ajaxResponse.setError(error);
                                }
                            } else {
                                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not find data for map."));
                            }
                        } else if (mode.equalsIgnoreCase("unmapall")) {
                            if (datamap.containsKey(pageArea.toString()) && datamap.containsKey(DownloadMappingPageArea.PageArea.SampleSetMethod.toString())) {
                                error = DownloadMappingPage.doUnmapAll(this.getTranslationProcessor(), datamap, datamap.get(DownloadMappingPageArea.PageArea.SampleSetMethod.toString()), reloadList, this.policyDef);
                                if (error.length() == 0) {
                                    clearSelection = true;
                                } else {
                                    ajaxResponse.setError(error);
                                }
                            } else {
                                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not find data for map."));
                            }
                        }
                        JSONObject out = new JSONObject();
                        for (DownloadMappingPageArea.PageArea toreload : reloadList) {
                            String title;
                            if (!datamap.containsKey(toreload.toString())) continue;
                            int[] sel = null;
                            if (selectedMap.containsKey(toreload.toString())) {
                                sel = (int[])selectedMap.get(toreload.toString());
                            }
                            try {
                                title = jobTitles != null && jobTitles.has(toreload.toString()) ? jobTitles.getString(toreload.toString()) : toreload.toString();
                            }
                            catch (Exception e) {
                                title = toreload.toString();
                            }
                            this.processArea(toreload, datamap.get(toreload.toString()), componentdata, sel, samplesetmethod, out, clearSelection, pageContext, title);
                        }
                        ajaxResponse.addCallbackArgument("pagearea", pageArea.toString());
                        ajaxResponse.addCallbackArgument("data", out);
                    }
                    if (datamap.containsKey(pageArea.toString())) {
                        DataSet tabledata = datamap.get(pageArea.toString());
                        JSONObject out = new JSONObject();
                        int[] sel = null;
                        if (selectedMap.containsKey(pageArea.toString())) {
                            sel = (int[])selectedMap.get(pageArea.toString());
                        }
                        String error = "";
                        if (mode.equalsIgnoreCase("add")) {
                            String sdiworkiteminfo = ajaxResponse.getRequestParameter("sdiworkitem");
                            if (sdiworkiteminfo.length() > 0) {
                                DataSet sdiworkitem = new DataSet(sdiworkiteminfo, (com.labvantage.sapphire.services.ConnectionInfo)this.connectionInfo);
                                if (tabledata == null) {
                                    tabledata = new DataSet(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
                                    datamap.put(pageArea.toString(), tabledata);
                                }
                                if (componentdata == null) {
                                    componentdata = new DataSet(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
                                    componentsmap.put(pageArea.toString(), componentdata);
                                }
                                error = DownloadMappingPage.doAdd(tabledata, componentdata, sdiworkitem, (PageContext)pageContext);
                            } else {
                                if (tabledata == null) {
                                    tabledata = new DataSet(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
                                    datamap.put(pageArea.toString(), tabledata);
                                }
                                if (componentdata == null) {
                                    componentdata = new DataSet(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
                                    componentsmap.put(pageArea.toString(), componentdata);
                                }
                                String reagentLotIds = ajaxResponse.getRequestParameter("reagentlotids");
                                error = DownloadMappingPage.doAdd(tabledata, componentdata, reagentLotIds, (PageContext)pageContext);
                            }
                        } else if (mode.equalsIgnoreCase("remove")) {
                            String idcol = "";
                            idcol = pageArea == DownloadMappingPageArea.PageArea.UnknownSamples ? "sdiworkitemid" : "reagentlotid";
                            error = DownloadMappingPage.doRemove(idcol, tabledata, componentdata, (int[])selectedMap.get(pageArea.toString()));
                        }
                        if (error.length() == 0) {
                            this.processArea(pageArea, tabledata, componentdata, sel, samplesetmethod, out, false, pageContext, pageArea.toString());
                        } else {
                            ajaxResponse.setError(error);
                        }
                        ajaxResponse.addCallbackArgument("pagearea", pageArea.toString());
                        ajaxResponse.addCallbackArgument("data", out);
                    }
                    ajaxResponse.setError(this.getTranslationProcessor().translate("No valid data for page area."));
                }
                finally {
                    if (p != 3) {
                        JspFactory.getDefaultFactory().releasePageContext((PageContext)pageContext);
                    }
                }
            } else {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Servlet could not be found."));
            }
        } else {
            this.logger.error("No table or component data.");
        }
        ajaxResponse.print();
    }

    private void processArea(DownloadMappingPageArea.PageArea pageArea, DataSet tabledata, DataSet componentdata, int[] selected, PropertyList samplesetmethod, JSONObject out, boolean clearselection, PageContext pageContext, String title) {
        DownloadMappingPageArea dmpa = new DownloadMappingPageArea(pageArea, tabledata, pageContext, this.policyDef, title);
        if (selected != null) {
            dmpa.setSelected(selected);
        }
        boolean clearSelection = clearselection;
        if (pageArea == DownloadMappingPageArea.PageArea.SampleSetMethod) {
            if (samplesetmethod != null) {
                clearSelection = true;
            }
        } else if (tabledata != null) {
            clearSelection = true;
        }
        JSONArray selectedout = new JSONArray();
        if (!clearSelection) {
            for (int selectedItem : dmpa.getSelected()) {
                selectedout.put(selectedItem);
            }
        }
        JSONObject pagearea = new JSONObject();
        try {
            out.put(pageArea.toString(), pagearea);
            pagearea.put("html", dmpa.getHtml());
            pagearea.put("selected", selectedout);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

