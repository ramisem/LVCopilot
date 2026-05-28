/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.workflow.gwt.server.steprequests;

import com.labvantage.opal.actions.QCBatchReagentSync;
import com.labvantage.opal.qcbatch.QCPositioning;
import com.labvantage.sapphire.modules.workflow.gwt.server.steprequests.BaseStepRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class QCSequenceStep
extends BaseStepRequest {
    @Override
    public void executeRequest(CommandRequest commandRequest, CommandResponse commandResponse) throws SapphireException {
        String request = commandRequest.getString("request");
        try {
            if (request.equalsIgnoreCase("loadqcsamplelist")) {
                QueryProcessor queryProcessor = new QueryProcessor(this.sapphireConnection.getConnectionId());
                String qcmethodid = commandRequest.getString("qcmethodid");
                String qcmethodversionid = commandRequest.getString("qcmethodversionid");
                DataSet qcData = commandRequest.getDataSet("qcdata");
                String[] qcdatacols = qcData.getColumns();
                StringBuffer data = new StringBuffer();
                for (int i = 0; i < qcData.size(); ++i) {
                    StringBuffer rowdata = new StringBuffer();
                    for (int j = 0; j < qcdatacols.length; ++j) {
                        rowdata.append(",").append(qcData.getValue(i, qcdatacols[j]));
                    }
                    data.append("|").append(rowdata.substring(1));
                }
                QCPositioning qcPositioning = new QCPositioning();
                qcPositioning.setQueryProcessor(queryProcessor);
                qcPositioning.setMethodID(qcmethodid);
                qcPositioning.setMethodVersionID(qcmethodversionid);
                qcPositioning.setUnknownSampleColumns(StringUtil.arrayToString(qcdatacols, "|"));
                qcPositioning.setUnknownSamplesData(data.substring(1));
                qcPositioning.insertQCSamples();
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
                commandResponse.set("qcsequence", qcSequence);
            } else if (request.equalsIgnoreCase("saveqcbatch")) {
                DataSet qcSequence = commandRequest.getDataSet("qcsequence");
                String[] columns = qcSequence.getColumns();
                StringBuffer data = new StringBuffer();
                for (int i = 0; i < qcSequence.size(); ++i) {
                    StringBuffer rowdata = new StringBuffer();
                    for (int j = 0; j < columns.length; ++j) {
                        rowdata.append(",").append(qcSequence.getValue(i, columns[j]));
                    }
                    data.append("|").append(rowdata.substring(1));
                }
                ActionProcessor actionProcessor = new ActionProcessor(this.sapphireConnection.getConnectionId());
                HashMap<String, String> addSDI = new HashMap<String, String>();
                addSDI.put("sdcid", "QCBatch");
                addSDI.put("qcmethodid", commandRequest.getString("qcmethodid"));
                addSDI.put("qcmethodversionid", commandRequest.getString("qcmethodversionid"));
                addSDI.put("overrideautokey", "N");
                actionProcessor.processAction("AddSDI", "1", addSDI);
                String qcbatchid = (String)addSDI.get("newkeyid1");
                HashMap<String, String> applyQCMethod = new HashMap<String, String>();
                applyQCMethod.put("sdcid", "QCBatch");
                applyQCMethod.put("qcbatchid", qcbatchid);
                applyQCMethod.put("qcmethodid", commandRequest.getString("qcmethodid"));
                applyQCMethod.put("qcmethodversionid", commandRequest.getString("qcmethodversionid"));
                applyQCMethod.put("sdiworkitemids", commandRequest.getString("sdiworkitemids"));
                applyQCMethod.put("sdidataids", commandRequest.getString("sdidataids"));
                applyQCMethod.put("allsamples", data.substring(1));
                applyQCMethod.put("columns", StringUtil.arrayToString(columns, "|"));
                actionProcessor.processAction("ApplyQCMethod", "1", applyQCMethod);
                HashMap<String, String> reagentSync = new HashMap<String, String>();
                reagentSync.put("qcbatchid", qcbatchid);
                actionProcessor.processActionClass(QCBatchReagentSync.class.getName(), reagentSync, false);
                commandResponse.set("qcbatchid", qcbatchid);
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to process step request '" + request + "'. Reason:" + e.getMessage(), e);
        }
    }
}

