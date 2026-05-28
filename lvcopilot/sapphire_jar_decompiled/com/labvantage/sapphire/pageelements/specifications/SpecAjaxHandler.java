/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.specifications;

import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.SDIProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;

public class SpecAjaxHandler
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private static final String PROPERTY_INPUTLIST = "inputlist";
    private static final String PROPERTY_MODE = "mode";
    private static final String PROPERTY_SEPERATOR = "seperator";
    private static final String SDCID_PARAMLIST = "ParamList";
    private static final String LINK_LINKTABLEID = "paramlistitem";
    private static final String MODE_GETPARAMLISTITEMS = "getparamlistitems";
    private static final String[] COLUMNS_LIST = new String[]{"paramlistid", "paramlistversionid", "variantid", "paramid", "paramtype", "displayunits", "datatypes", "entrysdcid", "entryreftypeid", "displayformat"};
    private String inputList;
    private String mode;
    private String seperator;

    private boolean loadProperties(AjaxResponse ajaxResponse) {
        this.logInfo("loadProperties called...");
        try {
            this.inputList = ajaxResponse.getRequestParameter(PROPERTY_INPUTLIST, "");
            this.logDebug("inputList = " + this.inputList);
            this.mode = ajaxResponse.getRequestParameter(PROPERTY_MODE, "");
            this.logDebug("mode = " + this.mode);
            this.seperator = ajaxResponse.getRequestParameter(PROPERTY_SEPERATOR, "%3B");
            this.logDebug("seperator = " + this.seperator);
        }
        catch (Exception e) {
            this.mode = null;
            this.inputList = null;
            this.seperator = "%3B";
        }
        return this.inputList != null && this.inputList.length() > 0 && this.mode != null && this.mode.length() > 0;
    }

    private DataSet getParamData(String theInputList) {
        this.logInfo("getParamData called...");
        String[] inputArray = StringUtil.split(theInputList, this.seperator);
        String keyid1 = "";
        String keyid2 = "";
        String keyid3 = "";
        ArrayList<String> key1s = new ArrayList<String>();
        ArrayList<String> key2s = new ArrayList<String>();
        ArrayList<String> key3s = new ArrayList<String>();
        for (int index = 0; index < inputArray.length; ++index) {
            String[] rowArray = StringUtil.split(inputArray[index], "|");
            if (rowArray.length > 3) {
                if (keyid1.length() == 0) {
                    keyid1 = rowArray[0];
                    keyid2 = rowArray[2];
                    keyid3 = rowArray[3];
                } else {
                    keyid1 = keyid1 + ";" + rowArray[0];
                    keyid2 = keyid2 + ";" + rowArray[2];
                    keyid3 = keyid3 + ";" + rowArray[3];
                }
                key1s.add(rowArray[0]);
                key2s.add(rowArray[2]);
                key3s.add(rowArray[3]);
                continue;
            }
            this.logWarn("Could not find keyid1, keyid2 and keyid3 for " + inputArray[index] + ".");
        }
        this.logDebug("keyid1 = " + keyid1);
        this.logDebug("keyid2 = " + keyid2);
        this.logDebug("keyid3 = " + keyid3);
        SDIRequest sdirequest = new SDIRequest();
        sdirequest.setSDCid(SDCID_PARAMLIST);
        sdirequest.setKeyid1List(keyid1);
        sdirequest.setKeyid2List(keyid2);
        sdirequest.setKeyid3List(keyid3);
        sdirequest.setRequestItem(LINK_LINKTABLEID);
        SDIProcessor sdiProcessor = new SDIProcessor(this.getConnectionId());
        SDIData sdidata = sdiProcessor.getSDIData(sdirequest);
        this.logDebug("sdidata.getDatasets().size() = " + sdidata.getDatasets().size());
        DataSet paramlistdata = sdidata.getDataset(LINK_LINKTABLEID);
        if (paramlistdata != null) {
            paramlistdata.sort("usersequence");
            if (key1s.size() == key2s.size() && key2s.size() == key3s.size()) {
                int i;
                DataSet out = new DataSet();
                for (i = 0; i < paramlistdata.getColumnCount(); ++i) {
                    String id = paramlistdata.getColumnId(i);
                    out.addColumn(id, paramlistdata.getColumnType(id));
                }
                for (i = 0; i < key1s.size(); ++i) {
                    String key1 = key1s.get(i).toString();
                    String key2 = key2s.get(i).toString();
                    String key3 = key3s.get(i).toString();
                    for (int k = 0; k < paramlistdata.size(); ++k) {
                        if (!paramlistdata.getValue(k, "paramlistid", "").equals(key1) || !paramlistdata.getValue(k, "paramlistversionid", "").equals(key2) || !paramlistdata.getValue(k, "variantid", "").equals(key3)) continue;
                        out.copyRow(paramlistdata, k, 1);
                    }
                }
                return out;
            }
            return paramlistdata;
        }
        return null;
    }

    private void buildReturn(StringBuffer out, DataSet data) {
        int column;
        this.logInfo("buildReturn called...");
        StringBuffer row = new StringBuffer();
        for (column = 0; column < COLUMNS_LIST.length; ++column) {
            if (row.length() == 0) {
                row.append(COLUMNS_LIST[column]);
                continue;
            }
            row.append("|").append(COLUMNS_LIST[column]);
        }
        if (out.length() == 0) {
            out.append(row);
        } else {
            out.append("%3B").append(row);
        }
        for (int index = 0; index < data.getRowCount(); ++index) {
            row = new StringBuffer();
            for (column = 0; column < COLUMNS_LIST.length; ++column) {
                if (row.length() == 0) {
                    row.append(data.getValue(index, COLUMNS_LIST[column], ""));
                    continue;
                }
                row.append("|").append(data.getValue(index, COLUMNS_LIST[column], ""));
            }
            out.append("%3B").append(row);
        }
    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        this.logInfo("processRequest called...");
        StringBuffer out = new StringBuffer();
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "specLimits.handleCallbackParameterItemsResponse");
        if (this.loadProperties(ajaxResponse)) {
            if (this.mode.equalsIgnoreCase(MODE_GETPARAMLISTITEMS)) {
                DataSet paramdata = this.getParamData(this.inputList);
                if (paramdata != null) {
                    if (paramdata.getRowCount() > 0) {
                        this.logDebug("paramdata.getRowCount == " + paramdata.getRowCount());
                        this.buildReturn(out, paramdata);
                        if (out.length() > 0) {
                            this.logDebug("out = " + out.toString());
                        }
                    } else {
                        this.logError("No parameter list items found for parameter lists.");
                        ajaxResponse.setError("No parameter list items found for parameter lists.");
                    }
                } else {
                    this.logError("Could not obtain parameter list items.");
                    ajaxResponse.setError("Could not obtain parameter list items.");
                }
            } else {
                this.logError("An incorrect action was provided.");
                ajaxResponse.setError("An incorrect action was provided.");
            }
        } else {
            this.logError("Could not load the required properties.");
            ajaxResponse.setError("Could not load the required properties.");
        }
        if (this.debugErrorMsg != null && this.debugErrorMsg.length() > 0) {
            ajaxResponse.setError(this.debugErrorMsg);
        } else if (out.length() > 0) {
            ajaxResponse.addCallbackArgument("data", out.toString());
        } else {
            ajaxResponse.setError("No data.");
        }
        ajaxResponse.print();
    }
}

