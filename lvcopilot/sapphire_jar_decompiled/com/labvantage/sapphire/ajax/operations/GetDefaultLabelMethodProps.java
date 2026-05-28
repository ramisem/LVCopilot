/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.ajax.operations.BaseLabelMethodRequest;
import com.labvantage.sapphire.servlet.RequestProcessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GetDefaultLabelMethodProps
extends BaseLabelMethodRequest {
    String webPageId = "LV_LabelMethodPrompt";

    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) {
        String smsLabels;
        AjaxResponse ar = new AjaxResponse(req, resp);
        this.webPageId = ar.getRequestParameter("pageid", "LV_LabelMethodPrompt");
        String sdcId = ar.getRequestParameter("sdcid", "Sample");
        boolean smsLabelMode = ar.getRequestParameter("smslabelmode", "false").equalsIgnoreCase("true");
        String sampleid = ar.getRequestParameter("sampleid", "");
        DataSet printerDs = new DataSet();
        ConfigurationProcessor cp = this.getConfigurationProcessor();
        String method = this.getDefaultValue(sdcId, cp, "lastusedlabelmethod", "");
        String versionId = this.getDefaultValue(sdcId, cp, "lastusedlabelmethodversion", "1");
        String userprinter = cp.getProfileProperty("sysuser_config_accession_printer", "");
        String printer = this.getDefaultValue(sdcId, cp, "lastusedlabelprinter", userprinter);
        String smsLabelMethods = "";
        String smsLabelMethodVersions = "";
        if (smsLabelMode && (smsLabels = this.getSMSLabelMethodResponse(sampleid)).contains(",")) {
            String[] tmp = smsLabels.split(",");
            smsLabelMethods = tmp[0];
            smsLabelMethodVersions = tmp[1];
            tmp = smsLabelMethods.split(";");
            if (tmp.length > 0) {
                method = tmp[0];
            }
            if ((tmp = smsLabelMethodVersions.split(";")).length > 0) {
                versionId = tmp[0];
            }
        }
        ar.addCallbackArgument("labelmethodid", method);
        ar.addCallbackArgument("labelmethodversionid", versionId);
        ar.addCallbackArgument("printeraddressid", printer);
        if (!method.isEmpty()) {
            String sysuserId = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId();
            printerDs = this.getLabelMethodPrinters(sysuserId, method, versionId, req, resp);
            printerDs.sort("displayvalue");
        }
        SDIRequest sdiRequest = this.getLabelMethodSdiRequest(sdcId);
        SDIProcessor sdiProcessor = this.getSDIProcessor();
        DataSet labelDs = sdiProcessor.getSDIData(sdiRequest).getDataset("primary");
        ArrayList<DataSet> groupedLabels = labelDs.getGroupedDataSets("labelmethodid");
        DataSet newlabelDs = new DataSet();
        for (DataSet label : groupedLabels) {
            int r = newlabelDs.addRow();
            newlabelDs.setString(r, "labelmethodid", label.getString(0, "labelmethodid"));
            newlabelDs.setString(r, "labelmethodtype", label.getString(0, "labelmethodtype"));
            newlabelDs.setString(r, "labelmethodversionid", label.getString(0, "labelmethodversionid"));
            newlabelDs.setString(r, "versionstatus", label.getString(0, "versionstatus"));
            newlabelDs.setString(r, "printertype", label.getString(0, "printertype"));
            newlabelDs.setString(r, "value", label.getString(0, "labelmethodid"));
            newlabelDs.setString(r, "displayvalue", label.getString(0, "labelmethodid"));
            newlabelDs.setString(r, "sdcid", label.getString(0, "labelsdcid"));
        }
        boolean addScreenprinter = false;
        HashMap<String, String> findMap = new HashMap<String, String>();
        findMap.put("value", method);
        int labelRow = newlabelDs.findRow(findMap);
        if (labelRow > -1) {
            String labelType = newlabelDs.getString(labelRow, "labelmethodtype");
            addScreenprinter = labelType.equals("Jasper") || labelType.equals("Report");
        }
        newlabelDs.sort("displayvalue");
        if (addScreenprinter) {
            int row = printerDs.addRow();
            printerDs.setValue(row, "value", "***SCREENPRINTER***");
            printerDs.setValue(row, "displayvalue", this.getTranslationProcessor().translate("Open PDF"));
        }
        ar.addCallbackArgument("printers", printerDs);
        ar.addCallbackArgument("labels", newlabelDs);
        ar.addCallbackArgument("smslabelmethodid", smsLabelMethods);
        ar.addCallbackArgument("smslabelmethodversionid", smsLabelMethodVersions);
        ar.print();
    }

    private String getSMSLabelMethodResponse(String sampleid) {
        String labelmethodid = "";
        String labelmethodversionid = "";
        if (!sampleid.isEmpty()) {
            StringBuilder sql = new StringBuilder();
            sql.append("select s.s_sampleid, s.sampletypeid, ");
            SafeSQL safeSQL = new SafeSQL();
            sql.append("  (select sp.labelmethodid from s_eventdefstspecimendef sp where sp.s_eventdefid = sf.eventdefid and sp.s_specimendefid = sf.specimendefid and sp.s_sampletypeid = sf.sampletypeid) labelmethodid,");
            sql.append("  (select sp.labelmethodversionid from s_eventdefstspecimendef sp where sp.s_eventdefid = sf.eventdefid and sp.s_specimendefid = sf.specimendefid and sp.s_sampletypeid = sf.sampletypeid) labelmethodversionid");
            if (this.getConnectionProcessor().getSapphireConnection().isOracle()) {
                sql.append(" ,(select csp.labelmethodid from s_childsampleplanitem csp, s_samplemap sm where csp.s_childsampleplanitemid = sm.childsampleplanitemid and csp.s_childsampleplanid = sm.childsampleplanid and csp.s_childsampleplanversionid = sm.childsampleplanversionid and sm.destsampleid = s.s_sampleid and rownum = 1) childlabelmethodid");
                sql.append(" ,(select csp.labelmethodversionid from s_childsampleplanitem csp, s_samplemap sm where csp.s_childsampleplanitemid = sm.childsampleplanitemid and csp.s_childsampleplanid = sm.childsampleplanid and csp.s_childsampleplanversionid = sm.childsampleplanversionid and sm.destsampleid = s.s_sampleid and rownum = 1) childlabelmethodversionid");
            } else {
                sql.append(" ,(select top(1) csp.labelmethodid from s_childsampleplanitem csp, s_samplemap sm where csp.s_childsampleplanitemid = sm.childsampleplanitemid and csp.s_childsampleplanid = sm.childsampleplanid and csp.s_childsampleplanversionid = sm.childsampleplanversionid and sm.destsampleid = s.s_sampleid) childlabelmethodid");
                sql.append(" ,(select top(1) csp.labelmethodversionid from s_childsampleplanitem csp, s_samplemap sm where csp.s_childsampleplanitemid = sm.childsampleplanitemid and csp.s_childsampleplanid = sm.childsampleplanid and csp.s_childsampleplanversionid = sm.childsampleplanversionid and sm.destsampleid = s.s_sampleid) childlabelmethodversionid");
            }
            sql.append("  from s_sample s, s_samplefamily sf");
            sql.append(" where sf.s_samplefamilyid = s.samplefamilyid");
            sql.append(" and s.s_sampleid in ( ").append(safeSQL.addIn(sampleid, ";")).append(" )");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && !ds.isEmpty()) {
                HashSet<String> labelMethodSet = new HashSet<String>();
                HashSet<String> labelMethodVersionSet = new HashSet<String>();
                for (int i = 0; i < ds.size(); ++i) {
                    String methodid = ds.getString(i, "childlabelmethodid", ds.getString(i, "labelmethodid", ""));
                    if (!OpalUtil.isNotEmpty(methodid)) {
                        labelMethodSet.clear();
                        labelMethodVersionSet.clear();
                        break;
                    }
                    String methodversionid = ds.getString(i, "childlabelmethodversionid", ds.getString(i, "labelmethodversionid", "1"));
                    labelMethodSet.add(methodid);
                    labelMethodVersionSet.add(methodversionid);
                }
                if (!labelMethodSet.isEmpty()) {
                    labelmethodid = OpalUtil.toDelimitedString(labelMethodSet, ";");
                    labelmethodversionid = OpalUtil.toDelimitedString(labelMethodVersionSet, ";");
                    return labelmethodid + "," + labelmethodversionid;
                }
            }
        }
        return "";
    }

    private String getDefaultValue(String sdcId, ConfigurationProcessor cp, String profilepropertyLabelmethod, String defaultValue) {
        String method = cp.getProfileProperty(this.getSDCIdSpecificProfileProperty(profilepropertyLabelmethod, sdcId), defaultValue);
        if (method.isEmpty()) {
            method = cp.getProfileProperty(profilepropertyLabelmethod, "");
        }
        return method;
    }

    protected SDIRequest getLabelMethodSdiRequest(String sdcId) {
        try {
            RequestProcessor requestProcessor = new RequestProcessor(this.getConnectionProcessor().getConnectionid());
            PropertyListCollection columns = requestProcessor.getWebPageProperties(this.webPageId, this.getRequestContext()).getPropertyListNotNull("pagedata").getCollectionNotNull("columns");
            String queryorderby = "labelmethodid";
            String querywhere = null;
            String queryfrom = null;
            String sdcid = "LV_LabelMethod";
            PropertyList dropdowndefinition = columns.find("id", "labelmethodid").getPropertyList("dropdowndefinition");
            if (dropdowndefinition != null) {
                querywhere = dropdowndefinition.getProperty("querywhere");
                queryfrom = dropdowndefinition.getProperty("queryfrom");
                sdcid = dropdowndefinition.getProperty("sdcid");
                querywhere = querywhere.replace("[sdcid]", sdcId);
                queryorderby = dropdowndefinition.getProperty("queryorderby", queryorderby);
            }
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(sdcid);
            sdiRequest.setRequestItem("primary");
            sdiRequest.setQueryFrom(queryfrom);
            if (!querywhere.isEmpty()) {
                sdiRequest.setQueryWhere(querywhere);
            }
            if (!queryorderby.isEmpty()) {
                sdiRequest.setQueryOrderBy(queryorderby);
            }
            return sdiRequest;
        }
        catch (SapphireException e) {
            throw new RuntimeException(e);
        }
    }
}

