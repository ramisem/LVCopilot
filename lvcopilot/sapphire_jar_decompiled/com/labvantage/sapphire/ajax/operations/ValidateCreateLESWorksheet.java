/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class ValidateCreateLESWorksheet
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        try {
            int i;
            String cur;
            boolean diff;
            String cache;
            String[] analystArray;
            String qcbatchid = ajaxResponse.getRequestParameter("qcbatchid");
            String workorderid = ajaxResponse.getRequestParameter("workorderid");
            String existsSdcid = "";
            String existsKeyid1 = "";
            String templatesSdcid = "";
            String templatesKeyid1 = "";
            String templatesKeyid2 = "(null)";
            String templatesRule = "";
            boolean multipleitems = false;
            String message = "";
            StringBuffer templateid = new StringBuffer();
            StringBuffer templateversionid = new StringBuffer();
            boolean analystAssigned = false;
            if (qcbatchid.length() > 0) {
                String[] qcmethodid = StringUtil.split(ajaxResponse.getRequestParameter("qcmethodid"), "%3B");
                String[] qcmethodversionid = StringUtil.split(ajaxResponse.getRequestParameter("qcmethodversionid"), "%3B");
                existsSdcid = "QCBatch";
                existsKeyid1 = qcbatchid;
                templatesSdcid = "QCMethod";
                templatesKeyid1 = qcmethodid[0];
                templatesKeyid2 = qcmethodversionid[0];
            } else if (workorderid.length() > 0) {
                existsSdcid = "WorkOrderSDC";
                existsKeyid1 = workorderid;
                DataSet workorder = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sourcesdcid, sourcekeyid1, certificationinterval FROM workorder WHERE workorderid = ?", new Object[]{workorderid});
                if (workorder.getValue(0, "sourcesdcid").equals("Instrument")) {
                    templatesRule = workorder.getValue(0, "certificationinterval");
                    DataSet instrument = this.getQueryProcessor().getPreparedSqlDataSet("SELECT instrumentmodelid, instrumenttype FROM instrument WHERE instrumentid = ?", new Object[]{workorder.getValue(0, "sourcekeyid1")});
                    if (instrument.size() == 1) {
                        String instrumenttype = instrument.getValue(0, "instrumenttype");
                        String instrumentmodelid = instrument.getValue(0, "instrumentmodelid");
                        if (instrumentmodelid.length() > 0) {
                            templatesSdcid = "LV_InstrumentModel;LV_InstrumentType";
                            templatesKeyid1 = instrumentmodelid + ";" + instrumenttype;
                            templatesKeyid2 = instrumenttype + ";(null)";
                        } else if (instrumenttype.length() > 0) {
                            templatesSdcid = "LV_InstrumentType";
                            templatesKeyid1 = instrumenttype;
                        }
                        if (templatesSdcid.length() == 0 || templatesKeyid1.length() == 0) {
                            message = this.getTranslationProcessor().translate("Template source not found for workorder instrument");
                        }
                    } else {
                        message = this.getTranslationProcessor().translate("Instrument not found for workorder");
                    }
                }
            } else if (ajaxResponse.getRequestParameter("workitemid").length() > 0) {
                int i2;
                String[] workitemid = StringUtil.split(ajaxResponse.getRequestParameter("workitemid"), "%3B");
                String[] workitemversionid = StringUtil.split(ajaxResponse.getRequestParameter("workitemversionid"), "%3B");
                String[] sdiworkitemid = StringUtil.split(ajaxResponse.getRequestParameter("sdiworkitemid"), "%3B");
                analystArray = StringUtil.split(ajaxResponse.getRequestParameter("analysts"), "%3B");
                multipleitems = sdiworkitemid.length > 1;
                existsSdcid = "SDIWorkItem";
                existsKeyid1 = StringUtil.arrayToString(sdiworkitemid, ";");
                templatesSdcid = "WorkItem";
                templatesKeyid1 = workitemid[0];
                templatesKeyid2 = workitemversionid[0];
                cache = "";
                diff = false;
                for (i2 = 0; i2 < workitemid.length; ++i2) {
                    cur = workitemid[i2] + "|" + workitemversionid[i2];
                    if (cache.length() == 0) {
                        cache = cur;
                        continue;
                    }
                    if (cur.equalsIgnoreCase(cache)) continue;
                    diff = true;
                    break;
                }
                if (diff) {
                    message = this.getTranslationProcessor().translate("Please make sure selection all have the same test method.");
                }
                if (message.length() == 0 && ajaxResponse.getRequestParameter("analysts").length() > 0) {
                    for (i2 = 0; i2 < analystArray.length; ++i2) {
                        if (analystArray[i2].length() <= 0) continue;
                        analystAssigned = true;
                        break;
                    }
                }
            } else if (ajaxResponse.getRequestParameter("sampleid").length() > 0) {
                String[] sampleid = StringUtil.split(ajaxResponse.getRequestParameter("sampleid"), "%3B");
                String[] samplesubtypeid = StringUtil.split(ajaxResponse.getRequestParameter("samplesubtypeid"), "%3B");
                String[] sampletypeid = StringUtil.split(ajaxResponse.getRequestParameter("sampletypeid"), "%3B");
                existsSdcid = "Sample";
                existsKeyid1 = StringUtil.arrayToString(sampleid, ";");
                String[] fkeylinkColumnIds = new String[]{"samplesubtypeid", "sampletypeid"};
                HashMap<String, String[]> fkeyValuesMap = new HashMap<String, String[]>();
                fkeyValuesMap.put("samplesubtypeid", samplesubtypeid);
                fkeyValuesMap.put("sampletypeid", sampletypeid);
                templatesSdcid = "SampleType;SampleType";
                block10: for (i = 0; i < fkeylinkColumnIds.length; ++i) {
                    String[] fkeyValues = (String[])fkeyValuesMap.get(fkeylinkColumnIds[i]);
                    String cache2 = "";
                    for (int s = 0; s < sampleid.length; ++s) {
                        String fkeyValue = fkeyValues[s];
                        if (fkeyValue.length() <= 0) continue block10;
                        if (cache2.length() == 0) {
                            cache2 = fkeyValue;
                            continue;
                        }
                        if (!fkeyValue.equalsIgnoreCase(cache2)) continue block10;
                    }
                    if (templatesKeyid1.length() > 0) {
                        templatesKeyid1 = templatesKeyid1 + ";";
                    }
                    templatesKeyid1 = templatesKeyid1 + fkeyValues[0];
                }
                if (templatesKeyid1.length() == 0) {
                    message = this.getTranslationProcessor().translate("Please make sure all selected Samples have same Sample Sub Type or same Sample Type");
                }
            } else if (ajaxResponse.getRequestParameter("reagentlotid").length() > 0) {
                int i3;
                String[] reagentlotid = StringUtil.split(ajaxResponse.getRequestParameter("reagentlotid"), "%3B");
                String[] reagenttypeid = StringUtil.split(ajaxResponse.getRequestParameter("reagenttypeid"), "%3B");
                String[] reagenttypeversionid = StringUtil.split(ajaxResponse.getRequestParameter("reagenttypeversionid"), "%3B");
                analystArray = StringUtil.split(ajaxResponse.getRequestParameter("analysts"), "%3B");
                existsSdcid = "LV_ReagentLot";
                existsKeyid1 = StringUtil.arrayToString(reagentlotid, ";");
                templatesSdcid = "LV_ReagentType";
                templatesKeyid1 = reagenttypeid[0];
                templatesKeyid2 = reagenttypeversionid[0];
                cache = "";
                diff = false;
                for (i3 = 0; i3 < reagenttypeid.length; ++i3) {
                    cur = reagenttypeid[i3] + "|" + reagenttypeversionid[i3];
                    if (cache.length() == 0) {
                        cache = cur;
                        continue;
                    }
                    if (cur.equalsIgnoreCase(cache)) continue;
                    diff = true;
                    break;
                }
                if (diff) {
                    message = this.getTranslationProcessor().translate("Please make sure all the selected items have the same ReagentType.");
                }
                if (message.length() == 0 && ajaxResponse.getRequestParameter("analysts").length() > 0) {
                    for (i3 = 0; i3 < analystArray.length; ++i3) {
                        if (analystArray[i3].length() <= 0) continue;
                        analystAssigned = true;
                        break;
                    }
                }
            } else {
                message = this.getTranslationProcessor().translate("Unrecognized inputs for LES validation.");
            }
            if (message.length() == 0) {
                try {
                    String rsetid = this.getDAMProcessor().createRSet(existsSdcid, existsKeyid1, null, null);
                    try {
                        int count = this.getQueryProcessor().getPreparedCount("SELECT COUNT( d.worksheetid ) FROM worksheet w, worksheetsdi d, rsetitems r WHERE d.worksheetid=w.worksheetid AND d.worksheetversionid=w.worksheetversionid AND w.lesflag='Y' AND r.rsetid = ? AND r.sdcid = ? AND r.keyid1 = d.keyid1 AND d.sdcid = r.sdcid", new Object[]{rsetid, existsSdcid});
                        if (count > 0) {
                            String string = message = multipleitems ? "One or more selected items already have a worksheet." : "Selected item already has a worksheet.";
                        }
                        if (message.length() == 0) {
                            String[] templateSDCArray = StringUtil.split(templatesSdcid, ";");
                            String[] templateKeyId1Array = StringUtil.split(templatesKeyid1, ";");
                            String[] templateKeyId2Array = StringUtil.split(templatesKeyid2, ";");
                            for (i = 0; i < templateKeyId1Array.length; ++i) {
                                String templateKey2 = "(null)";
                                if (templateKeyId2Array.length > i) {
                                    templateKey2 = templateKeyId2Array[i];
                                }
                                if (this.getWorksheetTemplateId(templateSDCArray[i], templateKeyId1Array[i], templateKey2, templatesRule, templateid, templateversionid).length() > 0) break;
                            }
                            if (templateid.length() == 0) {
                                message = this.getTranslationProcessor().translate("No worksheet templates associated with this item");
                            }
                        }
                    }
                    finally {
                        this.getDAMProcessor().clearRSet(rsetid);
                    }
                }
                catch (SapphireException e) {
                    ajaxResponse.setError(e.getMessage());
                    throw new ServletException((Throwable)e);
                }
            }
            ajaxResponse.addCallbackArgument("templateid", templateid.length() > 0 ? templateid.substring(1) : "");
            ajaxResponse.addCallbackArgument("templateversionid", templateversionid.length() > 0 ? templateversionid.substring(1) : "");
            ajaxResponse.addCallbackArgument("message", message);
            ajaxResponse.addCallbackArgument("userlookuppage", ajaxResponse.getRequestParameter("userlookuppage"));
            ajaxResponse.addCallbackArgument("defferedcallback", ajaxResponse.getRequestParameter("defferedcallback"));
            ajaxResponse.addCallbackArgument("analystalreadyassigned", analystAssigned ? "Y" : "N");
        }
        finally {
            ajaxResponse.print();
        }
    }

    private String getWorksheetTemplateId(String templatesSdcid, String templatesKeyid1, String templatesKeyid2, String templatesRule, StringBuffer templateid, StringBuffer templateversionid) throws SapphireException {
        Object[] objectArray;
        QueryProcessor queryProcessor = this.getQueryProcessor();
        String string = "SELECT * FROM sdiworksheetrule WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ?" + (templatesRule.length() > 0 ? " AND worksheetrule = ?" : "");
        if (templatesRule.length() > 0) {
            Object[] objectArray2 = new Object[4];
            objectArray2[0] = templatesSdcid;
            objectArray2[1] = templatesKeyid1;
            objectArray2[2] = templatesKeyid2;
            objectArray = objectArray2;
            objectArray2[3] = templatesRule;
        } else {
            Object[] objectArray3 = new Object[3];
            objectArray3[0] = templatesSdcid;
            objectArray3[1] = templatesKeyid1;
            objectArray = objectArray3;
            objectArray3[2] = templatesKeyid2;
        }
        DataSet sdiWorksheetRule = queryProcessor.getPreparedSqlDataSet(string, objectArray);
        for (int i = 0; i < sdiWorksheetRule.size(); ++i) {
            templateid.append(";").append(sdiWorksheetRule.getValue(i, "worksheetid"));
            templateversionid.append(";").append(BaseELNAction.resolveVersion(this.getQueryProcessor(), sdiWorksheetRule.getValue(i, "worksheetid"), sdiWorksheetRule.getValue(i, "worksheetversionid"), "worksheet"));
        }
        return templateid.toString();
    }
}

