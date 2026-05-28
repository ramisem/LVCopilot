/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.workflow.stepdef;

import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class AZContentChooserAjaxUtil
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "StepHandler");
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid", "");
        String treatmenttypeid = ajaxResponse.getRequestParameter("treatmenttypeid", "");
        String auditreason = ajaxResponse.getRequestParameter("auditreason", "");
        String auditactivity = ajaxResponse.getRequestParameter("auditactivity", "");
        String auditsignedlag = ajaxResponse.getRequestParameter("auditsignedflag", "");
        boolean saveData = false;
        if (trackitemid.length() > 0 || treatmenttypeid.length() > 0) {
            saveData = true;
        }
        if (saveData) {
            String itemid = ajaxResponse.getRequestParameter("itemid", "");
            String treatmentArrays = ajaxResponse.getRequestParameter("treatmentarrays", "");
            if (itemid.length() > 0 || treatmentArrays.length() > 0) {
                DataSet ds;
                int i;
                PropertyList actionProps = new PropertyList();
                String[] itemIdArr = itemid.split(";");
                String errormsg = "";
                if (trackitemid.length() > 0) {
                    DataSet reagentDS = new DataSet();
                    reagentDS.addColumn("arrayid", 0);
                    reagentDS.addColumn("zonename", 0);
                    reagentDS.addColumn("content", 0);
                    reagentDS.addColumn("contentkeyid1", 0);
                    reagentDS.addColumn("quantity", 0);
                    reagentDS.addColumn("quantityunits", 0);
                    reagentDS.addColumn("conc", 0);
                    reagentDS.addColumn("concunits", 0);
                    reagentDS.addColumn("contentsdcid", 0);
                    reagentDS.addColumnValues("contentsdcid", 0, "TrackItemSDC", ";");
                    reagentDS.addColumnValues("arrayid", 0, itemid, ";");
                    reagentDS.addColumnValues("zonename", 0, ajaxResponse.getRequestParameter("zonename", ""), ";");
                    reagentDS.addColumnValues("content", 0, ajaxResponse.getRequestParameter("content", ""), ";");
                    reagentDS.addColumnValues("contentkeyid1", 0, trackitemid, ";");
                    reagentDS.addColumnValues("quantity", 0, ajaxResponse.getRequestParameter("loadvol", ""), ";");
                    reagentDS.addColumnValues("quantityunits", 0, ajaxResponse.getRequestParameter("volunits", ""), ";");
                    reagentDS.addColumnValues("conc", 0, ajaxResponse.getRequestParameter("conc", ""), ";");
                    reagentDS.addColumnValues("concunits", 0, ajaxResponse.getRequestParameter("concunits", ""), ";");
                    reagentDS.padColumn("contentsdcid");
                    ArrayList<DataSet> reagentAL = reagentDS.getGroupedDataSets("zonename");
                    for (i = 0; i < reagentAL.size(); ++i) {
                        actionProps.clear();
                        ds = reagentAL.get(i);
                        actionProps.setProperty("contentsdcid", ds.getColumnValues("contentsdcid", ";"));
                        actionProps.setProperty("contentkeyid1", ds.getColumnValues("contentkeyid1", ";"));
                        actionProps.setProperty("content", ds.getColumnValues("content", ";"));
                        actionProps.setProperty("volume", ds.getColumnValues("quantity", ";"));
                        actionProps.setProperty("volumeunits", ds.getColumnValues("quantityunits", ";"));
                        actionProps.setProperty("concentration", ds.getColumnValues("conc", ";"));
                        actionProps.setProperty("concentrationunits", ds.getColumnValues("concunits", ";"));
                        actionProps.setProperty("zonename", ds.getValue(0, "zonename", ""));
                        actionProps.setProperty("arrayid", ds.getValue(0, "arrayid", ""));
                        actionProps.setProperty("level", "zone");
                        actionProps.setProperty("auditreason", auditreason);
                        actionProps.setProperty("auditactivity", auditactivity);
                        actionProps.setProperty("auditsignedflag", auditsignedlag);
                        try {
                            this.getActionProcessor().processAction("LoadArray", "1", actionProps);
                            continue;
                        }
                        catch (SapphireException e) {
                            this.logger.error("Failed to update Array Content", e);
                            ajaxResponse.setError(e.getMessage());
                            ajaxResponse.print();
                            return;
                        }
                    }
                }
                if (errormsg.length() == 0 && treatmenttypeid.length() > 0) {
                    DataSet treatmentDS = new DataSet();
                    treatmentDS.addColumn("arrayid", 0);
                    treatmentDS.addColumn("zonename", 0);
                    treatmentDS.addColumn("treatmentcontent", 0);
                    treatmentDS.addColumn("contentkeyid1", 0);
                    treatmentDS.addColumnValues("arrayid", 0, treatmentArrays, ";");
                    treatmentDS.addColumnValues("zonename", 0, ajaxResponse.getRequestParameter("treatmentzone", ""), ";");
                    treatmentDS.addColumnValues("treatmentcontent", 0, ajaxResponse.getRequestParameter("treatmentcontent", ""), ";");
                    treatmentDS.addColumnValues("contentkeyid1", 0, treatmenttypeid, ";");
                    ArrayList<DataSet> treatmentAL = treatmentDS.getGroupedDataSets("arrayid");
                    for (i = 0; i < treatmentAL.size(); ++i) {
                        actionProps.clear();
                        ds = treatmentAL.get(i);
                        actionProps.setProperty("contentsdcid", "LV_Treatment");
                        actionProps.setProperty("contentkeyid1", ds.getColumnValues("contentkeyid1", ";"));
                        actionProps.setProperty("content", ds.getColumnValues("treatmentcontent", ";"));
                        actionProps.setProperty("arrayid", ds.getValue(0, "arrayid"));
                        actionProps.setProperty("zonename", ds.getColumnValues("zonename", ";"));
                        actionProps.setProperty("level", "zone");
                        actionProps.setProperty("volume", ds.getColumnValues("quantity", ";"));
                        actionProps.setProperty("volumeunits", ds.getColumnValues("quantityunits", ";"));
                        actionProps.setProperty("concentration", ds.getColumnValues("conc", ";"));
                        actionProps.setProperty("concentrationunits", ds.getColumnValues("concunits", ";"));
                        actionProps.setProperty("auditreason", auditreason);
                        actionProps.setProperty("auditactivity", auditactivity);
                        actionProps.setProperty("auditsignedflag", auditsignedlag);
                        try {
                            this.getActionProcessor().processAction("LoadArray", "1", actionProps);
                            continue;
                        }
                        catch (Exception e) {
                            this.logger.error("Failed to update Array Content", e);
                            errormsg = e.getMessage();
                        }
                    }
                }
                if (errormsg.length() > 0) {
                    ajaxResponse.setError(errormsg);
                } else {
                    ajaxResponse.addCallbackArgument("resp", "Data saved successfully");
                }
            } else {
                ajaxResponse.setError(this.getTranslationProcessor().translate("No Item Id provided."));
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No Track Item Id provided."));
        }
        ajaxResponse.print();
    }
}

