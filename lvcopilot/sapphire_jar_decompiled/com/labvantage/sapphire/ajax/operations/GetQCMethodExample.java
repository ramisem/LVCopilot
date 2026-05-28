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

import com.labvantage.opal.exception.QCPositioningException;
import com.labvantage.opal.qcbatch.QCPositioning;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class GetQCMethodExample
extends BaseAjaxRequest {
    private final String[] qcDataColumns = new String[]{"usersequence", "linktoqcbatchitemid", "keyid1", "qcsampletype", "qcbatchsampletypeid", "qcbatchitemdesc", "linkedto", "s_qcbatchid", "s_qcbatchitemid"};

    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        int nofSamples;
        AjaxResponse ar = new AjaxResponse(req, resp);
        StringBuilder message = new StringBuilder();
        String qcmethodId = ar.getRequestParameter("qcmethodid", "");
        String qcmethodVersionId = ar.getRequestParameter("qcmethodversionid", "1");
        String nofSamplesStr = ar.getRequestParameter("nofsamples", "10");
        try {
            nofSamples = Integer.parseInt(nofSamplesStr);
        }
        catch (Exception e) {
            nofSamples = 10;
        }
        if (qcmethodId.equals("")) {
            message.append(this.getTranslationProcessor().translate("No QC-Method provided"));
        }
        String[] selectedItems = new String[nofSamples];
        for (int i = 0; i < nofSamples; ++i) {
            selectedItems[i] = this.getTranslationProcessor().translate("Sample") + " " + (i + 1);
        }
        DataSet qcData = new DataSet();
        for (String key : this.qcDataColumns) {
            qcData.addColumn(key, 0);
        }
        for (int i = 0; i < selectedItems.length; ++i) {
            if (selectedItems[i].length() <= 0) continue;
            int row = qcData.addRow();
            qcData.setValue(row, "usersequence", String.valueOf(i + 1));
            qcData.setValue(row, "keyid1", selectedItems[i]);
            qcData.setValue(row, "qcsampletype", "Unknown");
        }
        String[] qcdatacols = qcData.getColumns();
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < qcData.size(); ++i) {
            StringBuilder rowdata = new StringBuilder();
            for (String qcdatacol : qcdatacols) {
                rowdata.append(",").append(qcData.getValue(i, qcdatacol));
            }
            data.append("|").append(rowdata.substring(1));
        }
        QCPositioning qcPositioning = new QCPositioning();
        qcPositioning.setQueryProcessor(this.getQueryProcessor());
        qcPositioning.setMethodID(qcmethodId);
        qcPositioning.setMethodVersionID(qcmethodVersionId);
        qcPositioning.setUnknownSampleColumns(StringUtil.arrayToString(qcdatacols, "|"));
        qcPositioning.setUnknownSamplesData(data.substring(1));
        try {
            qcPositioning.insertQCSamples();
        }
        catch (QCPositioningException e) {
            message.append("Could not create AQC Batch");
        }
        String[] finalrows = StringUtil.split(qcPositioning.getFinalSamplesList(), "|");
        DataSet qcSequence = new DataSet();
        for (int i = 0; i < finalrows.length; ++i) {
            int row = qcSequence.addRow();
            String[] finalcols = StringUtil.split(finalrows[i], ",");
            for (int j = 0; j < qcdatacols.length; ++j) {
                if (qcdatacols[j].equals("usersequence")) {
                    qcSequence.setString(row, qcdatacols[j], String.valueOf(i + 1));
                    continue;
                }
                if (qcdatacols[j].equals("linkedto")) {
                    qcSequence.setString(row, qcdatacols[j], finalcols[j]);
                    if (finalcols[j] != null && finalcols[j].length() > 0) {
                        int linkedPosn;
                        if (finalcols[j].startsWith("+") || finalcols[j].startsWith("-")) {
                            linkedPosn = i + 1 + new Integer(finalcols[j]);
                            qcSequence.setString(row, "linktoqcbatchitemid", String.valueOf(linkedPosn));
                            continue;
                        }
                        linkedPosn = new Integer(finalcols[j]);
                        qcSequence.setString(row, "linktoqcbatchitemid", String.valueOf(linkedPosn));
                        continue;
                    }
                    qcSequence.setString(row, "linktoqcbatchitemid", "");
                    continue;
                }
                if (qcdatacols[j].equals("linktoqcbatchitemid")) continue;
                qcSequence.setString(row, qcdatacols[j], finalcols[j]);
            }
        }
        JSONObject qcSeq = qcSequence.toJSONObject();
        ar.addCallbackArgument("qcSequence", qcSeq.toString());
        ar.addCallbackArgument("msg", message);
        ar.addCallbackArgument("nofsamples", nofSamples);
        ar.print();
    }
}

