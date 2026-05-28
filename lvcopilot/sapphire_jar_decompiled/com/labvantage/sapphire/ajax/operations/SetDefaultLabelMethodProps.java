/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.ajax.operations.BaseLabelMethodRequest;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.util.DataSet;

public class SetDefaultLabelMethodProps
extends BaseLabelMethodRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) {
        AjaxResponse ar = new AjaxResponse(req, resp);
        String message = "";
        ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(this.getConnectionId());
        String sdcId = ar.getRequestParameter("sdcid", "Sample");
        String printer = ar.getRequestParameter("printeraddressid", "");
        String method = ar.getRequestParameter("labelmethodid", "");
        String versionid = ar.getRequestParameter("labelmethodversionid", "1");
        DataSet ds = new DataSet();
        String sysuserId = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId();
        if (!printer.isEmpty()) {
            try {
                configurationProcessor.setProfileProperty(sysuserId, this.getSDCIdSpecificProfileProperty("lastusedlabelprinter", sdcId), printer);
            }
            catch (SapphireException e) {
                message = "Could not store printer as last used printer";
            }
        }
        if (!method.isEmpty()) {
            try {
                configurationProcessor.setProfileProperty(sysuserId, this.getSDCIdSpecificProfileProperty("lastusedlabelmethod", sdcId), method);
                configurationProcessor.setProfileProperty(sysuserId, this.getSDCIdSpecificProfileProperty("lastusedlabelmethodversion", sdcId), versionid);
            }
            catch (SapphireException e) {
                message = "Could not store method as last used label method";
            }
            ds = this.getLabelMethodPrinters(sysuserId, method, versionid, req, resp);
            ds.sort("displayvalue");
            String sql = this.getLabelMethodSql();
            Object[] params = new String[]{sdcId};
            DataSet labelDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
            boolean addScreenprinter = false;
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("value", method);
            int labelRow = labelDs.findRow(findMap);
            if (labelRow > -1) {
                String labelType = labelDs.getString(labelRow, "labelmethodtype");
                boolean bl = addScreenprinter = labelType.equals("Jasper") || labelType.equals("Report");
            }
            if (addScreenprinter) {
                int row = ds.addRow();
                ds.setValue(row, "value", "***SCREENPRINTER***");
                ds.setValue(row, "displayvalue", this.getTranslationProcessor().translate("Open PDF"));
            }
        }
        ar.addCallbackArgument("message", message);
        ar.addCallbackArgument("printers", ds);
        ar.print();
    }

    protected String getLabelMethodSql() {
        return "SELECT distinct labelmethodid as value, coalesce(labelmethoddesc, labelmethodid) as displayvalue, printertype, 'LV_LabelMethod' as sdcid, labelmethodtype  FROM labelmethod WHERE  labelmethod.labelsdcid = ? AND  coalesce(activeflag, 'Y') = 'Y' " + this.getSecurityWhere("LV_LabelMethod");
    }
}

