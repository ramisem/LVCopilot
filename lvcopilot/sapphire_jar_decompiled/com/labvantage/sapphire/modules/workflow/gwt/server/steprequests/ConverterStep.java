/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.workflow.gwt.server.steprequests;

import com.labvantage.sapphire.modules.workflow.gwt.server.steprequests.BaseStepRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIList;

public class ConverterStep
extends BaseStepRequest {
    @Override
    public void executeRequest(CommandRequest commandRequest, CommandResponse commandResponse) throws SapphireException {
        String inputsdcid = commandRequest.getString("inputsdcid");
        SDIList inputSDIList = null;
        if (commandRequest.getString("inputvariabletype").equalsIgnoreCase("sdilist")) {
            inputSDIList = commandRequest.getSDIList("inputvariable");
        } else {
            inputSDIList = new SDIList();
            inputSDIList.addSDIList(commandRequest.getString("inputvariable"));
        }
        inputSDIList.setSdcid(inputsdcid);
        String outputsdcid = commandRequest.getString("outputsdcid");
        SDIList outputSDIList = new SDIList();
        outputSDIList.setSdcid(outputsdcid);
        ConverterStep.convertSDIList(new SDCProcessor(this.sapphireConnection.getConnectionId()), new QueryProcessor(this.sapphireConnection.getConnectionId()), new DAMProcessor(this.sapphireConnection.getConnectionId()), inputSDIList, outputSDIList, commandRequest.getString("conversiontype"), commandRequest.getString("columnid"), commandRequest.getString("sql"));
        commandResponse.set("outputvariable", outputSDIList);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static void convertSDIList(SDCProcessor sdcProcessor, QueryProcessor queryProcessor, DAMProcessor damProcessor, SDIList inputSDIList, SDIList outputSDIList, String conversiontype, String columnid, String sql) throws SapphireException {
        String rsetid = null;
        try {
            String inputsdcid = inputSDIList.getSdcid();
            String outputsdcid = outputSDIList.getSdcid();
            rsetid = damProcessor.createRSet(inputsdcid, inputSDIList.getKeyid1(), inputSDIList.getKeyid2(), inputSDIList.getKeyid3());
            String inputtableid = sdcProcessor.getProperty(inputsdcid, "tableid");
            String inputkeycolid1 = sdcProcessor.getProperty(inputsdcid, "keycolid1");
            String inputkeycolid2 = sdcProcessor.getProperty(inputsdcid, "keycolid2");
            String inputkeycolid3 = sdcProcessor.getProperty(inputsdcid, "keycolid3");
            if (conversiontype.equalsIgnoreCase("FK")) {
                DataSet linkdata = sdcProcessor.getLinksData(inputsdcid);
                HashMap<String, String> findMap = new HashMap<String, String>();
                findMap.put("linktype", "F");
                findMap.put("linksdcid", outputsdcid);
                int findRow = linkdata.findRow(findMap);
                if (findRow > -1) {
                    if (linkdata.findRow(findMap, findRow + 1) > -1) {
                        findMap.put("sdccolumnid", columnid);
                        findRow = linkdata.findRow(findMap);
                    }
                    if (findRow > -1) {
                        DataSet data = queryProcessor.getPreparedSqlDataSet("SELECT DISTINCT " + inputtableid + "." + linkdata.getValue(findRow, "sdccolumnid") + " " + (linkdata.getValue(findRow, "sdccolumnid2").length() > 0 ? inputtableid + "." + linkdata.getValue(findRow, "sdccolumnid2") + " " : "") + (linkdata.getValue(findRow, "sdccolumnid3").length() > 0 ? inputtableid + "." + linkdata.getValue(findRow, "sdccolumnid3") + " " : "") + "FROM rsetitems, " + inputtableid + " WHERE rsetitems.rsetid = ?  AND " + inputtableid + "." + linkdata.getValue(findRow, "sdccolumnid") + " IS NOT NULL  AND  rsetitems.keyid1 = " + inputtableid + "." + inputkeycolid1 + (inputkeycolid2.length() > 0 ? " AND rsetitems.keyid2 = " + inputtableid + "." + inputkeycolid2 : "") + (inputkeycolid3.length() > 0 ? " AND rsetitems.keyid3 = " + inputtableid + "." + inputkeycolid3 : "") + " ORDER BY 1", new Object[]{rsetid});
                        outputSDIList.addSDIList(data.getColumnValues(linkdata.getValue(findRow, "sdccolumnid"), ";"), linkdata.getValue(findRow, "sdccolumnid2").length() > 0 ? data.getColumnValues(linkdata.getValue(findRow, "sdccolumnid2"), ";") : "", linkdata.getValue(findRow, "sdccolumnid3").length() > 0 ? data.getColumnValues(linkdata.getValue(findRow, "sdccolumnid3"), ";") : "");
                    }
                }
            } else if (conversiontype.equalsIgnoreCase("MM")) {
                DataSet linkdata = sdcProcessor.getLinksData(inputsdcid);
                HashMap<String, String> findMap = new HashMap<String, String>();
                findMap.put("linktype", "M");
                findMap.put("linksdcid", outputsdcid);
                int findRow = linkdata.findRow(findMap);
                if (findRow > -1) {
                    if (linkdata.findRow(findMap, findRow + 1) > -1) {
                        findMap.put("sdccolumnid", columnid);
                        findRow = linkdata.findRow(findMap);
                    }
                    if (findRow > -1) {
                        String outputkeycolid1 = sdcProcessor.getProperty(outputsdcid, "keycolid1");
                        String outputkeycolid2 = sdcProcessor.getProperty(outputsdcid, "keycolid2");
                        String outputkeycolid3 = sdcProcessor.getProperty(outputsdcid, "keycolid3");
                        String mmtableid = linkdata.getValue(findRow, "linktableid");
                        DataSet data = queryProcessor.getPreparedSqlDataSet("SELECT DISTINCT " + mmtableid + "." + outputkeycolid1 + " " + (outputkeycolid2.length() > 0 ? ", " + mmtableid + "." + outputkeycolid2 + " " : "") + (outputkeycolid3.length() > 0 ? ", " + mmtableid + "." + outputkeycolid3 + " " : "") + "FROM rsetitems, " + mmtableid + " WHERE rsetitems.rsetid = ?  AND  rsetitems.keyid1 = " + mmtableid + "." + inputkeycolid1 + (inputkeycolid2.length() > 0 ? " AND rsetitems.keyid2 = " + mmtableid + "." + inputkeycolid2 : "") + (inputkeycolid3.length() > 0 ? " AND rsetitems.keyid3 = " + mmtableid + "." + inputkeycolid3 : "") + " ORDER BY 1", new Object[]{rsetid});
                        outputSDIList.addSDIList(data.getColumnValues(outputkeycolid1, ";"), outputkeycolid2.length() > 0 ? data.getColumnValues(outputkeycolid2, ";") : "", outputkeycolid3.length() > 0 ? data.getColumnValues(outputkeycolid3, ";") : "");
                    }
                }
            } else if (conversiontype.equalsIgnoreCase("RK")) {
                DataSet reverselinkdata = sdcProcessor.getReverseLinksData(inputsdcid);
                HashMap<String, String> findMap = new HashMap<String, String>();
                findMap.put("linktype", "F");
                findMap.put("sdcid", outputsdcid);
                int findRow = reverselinkdata.findRow(findMap);
                if (findRow > -1) {
                    if (reverselinkdata.findRow(findMap, findRow + 1) > -1) {
                        findMap.put("sdccolumnid", columnid);
                        findRow = reverselinkdata.findRow(findMap);
                    }
                    if (findRow > -1) {
                        String linktableid = sdcProcessor.getProperty(outputsdcid, "tableid");
                        String linkkeycolid1 = sdcProcessor.getProperty(outputsdcid, "keycolid1");
                        String linkkeycolid2 = sdcProcessor.getProperty(outputsdcid, "keycolid2");
                        String linkkeycolid3 = sdcProcessor.getProperty(outputsdcid, "keycolid3");
                        DataSet data = queryProcessor.getPreparedSqlDataSet("SELECT DISTINCT " + linktableid + "." + linkkeycolid1 + " " + (linkkeycolid2.length() > 0 ? ", " + linktableid + "." + linkkeycolid2 + " " : "") + (linkkeycolid3.length() > 0 ? ", " + linktableid + "." + linkkeycolid3 + " " : "") + "FROM rsetitems, " + linktableid + " WHERE rsetitems.rsetid = ?  AND  rsetitems.keyid1 = " + linktableid + "." + reverselinkdata.getValue(findRow, "sdccolumnid") + (reverselinkdata.getValue(findRow, "sdccolumnid2").length() > 0 ? " AND rsetitems.keyid2 = " + linktableid + "." + reverselinkdata.getValue(findRow, "sdccolumnid2") : "") + (reverselinkdata.getValue(findRow, "sdccolumnid3").length() > 0 ? " AND rsetitems.keyid3 = " + linktableid + "." + reverselinkdata.getValue(findRow, "sdccolumnid3") : "") + " ORDER BY 1", new Object[]{rsetid});
                        outputSDIList.addSDIList(data.getColumnValues(linkkeycolid1, ";"), linkkeycolid2.length() > 0 ? data.getColumnValues(linkkeycolid2, ";") : "", linkkeycolid3.length() > 0 ? data.getColumnValues(linkkeycolid3, ";") : "");
                    }
                }
            } else if (conversiontype.equalsIgnoreCase("WI")) {
                if (inputsdcid.equals("SDIWorkItem")) {
                    int keycols = Integer.parseInt(sdcProcessor.getProperty(outputsdcid, "keycolumns"));
                    DataSet data = queryProcessor.getPreparedSqlDataSet("SELECT DISTINCT sdiworkitem.keyid1 " + (keycols > 1 ? ",sdiworkitem.keyid2 " : "") + (keycols > 2 ? ",sdiworkitem.keyid3 " : "") + "FROM rsetitems, sdiworkitem WHERE rsetitems.rsetid = ?  AND  rsetitems.keyid1 = sdiworkitem.sdiworkitemid ORDER BY 1", new Object[]{rsetid});
                    outputSDIList.addSDIList(data.getColumnValues("keyid1", ";"), keycols > 1 ? data.getColumnValues("keyid2", ";") : "", keycols > 2 ? data.getColumnValues("keyid3", ";") : "");
                } else {
                    if (!outputsdcid.equals("SDIWorkItem")) throw new SapphireException("SDIWorkItem conversion must have SDIWorkItem as either the input or output SDC");
                    int keycols = Integer.parseInt(sdcProcessor.getProperty(inputsdcid, "keycolumns"));
                    DataSet data = queryProcessor.getPreparedSqlDataSet("SELECT DISTINCT sdiworkitem.sdiworkitemid FROM rsetitems, sdiworkitem WHERE rsetitems.rsetid = ? AND  rsetitems.sdcid = sdiworkitem.sdcid AND  rsetitems.keyid1 = sdiworkitem.keyid1 " + (keycols > 1 ? "AND rsetitems.keyid2 = sdiworkitem.keyid2 " : "") + (keycols > 2 ? "AND rsetitems.keyid3 = sdiworkitem.keyid3 " : "") + "ORDER BY 1", new Object[]{rsetid});
                    outputSDIList.addSDIList(data.getColumnValues("sdiworkitemid", ";"));
                }
            } else if (conversiontype.equalsIgnoreCase("QC")) {
                if (inputsdcid.equals("QCBatch")) {
                    int keycols = Integer.parseInt(sdcProcessor.getProperty(outputsdcid, "keycolumns"));
                    DataSet data = queryProcessor.getPreparedSqlDataSet("SELECT DISTINCT sdidata.keyid1 " + (keycols > 1 ? ",sdidata.keyid2 " : "") + (keycols > 2 ? ",sdidata.keyid3 " : "") + "FROM rsetitems, sdidata WHERE rsetitems.rsetid = ?  AND  rsetitems.keyid1 = sdidata.s_qcbatchid ORDER BY 1", new Object[]{rsetid});
                    outputSDIList.addSDIList(data.getColumnValues("keyid1", ";"), keycols > 1 ? data.getColumnValues("keyid2", ";") : "", keycols > 2 ? data.getColumnValues("keyid3", ";") : "");
                } else {
                    if (!outputsdcid.equals("QCBatch")) throw new SapphireException("QCBatch conversion must have QCBatch as either the input or output SDC");
                    int keycols = Integer.parseInt(sdcProcessor.getProperty(inputsdcid, "keycolumns"));
                    DataSet data = queryProcessor.getPreparedSqlDataSet("SELECT DISTINCT sdidata.s_qcbatchid FROM rsetitems, sdidata WHERE rsetitems.rsetid = ? AND  rsetitems.sdcid = sdidata.sdcid AND  rsetitems.keyid1 = sdidata.keyid1 " + (keycols > 1 ? "AND rsetitems.keyid2 = sdidata.keyid2 " : "") + (keycols > 2 ? "AND rsetitems.keyid3 = sdidata.keyid3 " : "") + "ORDER BY 1", new Object[]{rsetid});
                    outputSDIList.addSDIList(data.getColumnValues("s_qcbatchid", ";"));
                }
            } else if (conversiontype.equalsIgnoreCase("QCWI")) {
                if (inputsdcid.equals("QCBatch")) {
                    if (!outputsdcid.equals("SDIWorkItem")) {
                        throw new SapphireException("QCBatch WorkItem conversion must have SDIWorkItem as the output SDC when QCBatch is the input SDC");
                    }
                    DataSet data = queryProcessor.getPreparedSqlDataSet("SELECT DISTINCT sdiworkitem.sdiworkitemid  FROM rsetitems, sdidata, sdiworkitem WHERE rsetitems.rsetid = ?  AND  rsetitems.keyid1 = sdidata.s_qcbatchid  AND  sdidata.sdcid = sdiworkitem.sdcid AND sdidata.keyid1 = sdiworkitem.keyid1 AND sdidata.keyid2 = sdiworkitem.keyid2 AND sdidata.keyid3 = sdiworkitem.keyid3  AND  sdidata.sourceworkitemid = sdiworkitem.workitemid AND sdidata.sourceworkiteminstance = sdiworkitem.workiteminstance ORDER BY 1", new Object[]{rsetid});
                    outputSDIList.addSDIList(data.getColumnValues("sdiworkitemid", ";"), "", "");
                } else {
                    if (!outputsdcid.equals("QCBatch")) throw new SapphireException("QCBatch conversion must have QCBatch as either the input or output SDC");
                    if (!inputsdcid.equals("SDIWorkItem")) {
                        throw new SapphireException("QCBatch WorkItem conversion must have as SDIWorkItem the input SDC when QCBatch is the output SDC");
                    }
                    DataSet data = queryProcessor.getPreparedSqlDataSet("SELECT DISTINCT sdidata.s_qcbatchid FROM rsetitems, sdidata, sdiworkitem WHERE rsetitems.rsetid = ? AND  rsetitems.keyid1 = sdiworkitem.sdiworkitemid  AND  sdidata.sdcid = sdiworkitem.sdcid AND sdidata.keyid1 = sdiworkitem.keyid1 AND sdidata.keyid2 = sdiworkitem.keyid2 AND sdidata.keyid3 = sdiworkitem.keyid3  AND  sdidata.sourceworkitemid = sdiworkitem.workitemid AND sdidata.sourceworkiteminstance = sdiworkitem.workiteminstance ORDER BY 1", new Object[]{rsetid});
                    outputSDIList.addSDIList(data.getColumnValues("s_qcbatchid", ";"));
                }
            } else if (conversiontype.equalsIgnoreCase("TI")) {
                if (inputsdcid.equals("TrackItemSDC")) {
                    int keycols = Integer.parseInt(sdcProcessor.getProperty(outputsdcid, "keycolumns"));
                    DataSet data = queryProcessor.getPreparedSqlDataSet("SELECT DISTINCT linkkeyid1 " + (keycols > 1 ? ",linkkeyid2 " : "") + (keycols > 2 ? ",linkkeyid3 " : "") + "FROM rsetitems, trackitem WHERE rsetitems.rsetid = ?  AND  rsetitems.keyid1 = trackitem.trackitemid ORDER BY 1", new Object[]{rsetid});
                    outputSDIList.addSDIList(data.getColumnValues("keyid1", ";"), keycols > 1 ? data.getColumnValues("keyid2", ";") : "", keycols > 2 ? data.getColumnValues("keyid3", ";") : "");
                } else {
                    if (!outputsdcid.equals("TrackItemSDC")) throw new SapphireException("TrackItem conversion must have TrackItemSDC as either the input or output SDC");
                    int keycols = Integer.parseInt(sdcProcessor.getProperty(inputsdcid, "keycolumns"));
                    DataSet data = queryProcessor.getPreparedSqlDataSet("SELECT DISTINCT trackitem.trackitemid FROM rsetitems, trackitem WHERE rsetitems.rsetid = ? AND  rsetitems.sdcid = trackitem.linksdcid AND  rsetitems.keyid1 = trackitem.linkkeyid1 " + (keycols > 1 ? "AND rsetitems.keyid2 = trackitem.linkkeyid2 " : "") + (keycols > 2 ? "AND rsetitems.keyid3 = trackitem.linkkeyid3 " : "") + "ORDER BY 1", new Object[]{rsetid});
                    outputSDIList.addSDIList(data.getColumnValues("trackitemid", ";"));
                }
            } else if (conversiontype.equalsIgnoreCase("SQL")) {
                if (sql.length() == 0) {
                    throw new SapphireException("Missing SQL");
                }
                DataSet data = queryProcessor.getPreparedSqlDataSet(sql, new Object[]{rsetid});
                String[] columns = data.getColumns();
                outputSDIList.addSDIList(data.getColumnValues(columns[0], ";"), columns.length > 1 ? data.getColumnValues(columns[1], ";") : "", columns.length > 2 ? data.getColumnValues(columns[2], ";") : "");
            } else if (conversiontype.equalsIgnoreCase("SU")) {
                if (inputsdcid.equals("StorageUnitSDC")) {
                    int keycols = Integer.parseInt(sdcProcessor.getProperty(outputsdcid, "keycolumns"));
                    DataSet data = queryProcessor.getPreparedSqlDataSet("SELECT DISTINCT linkkeyid1 " + (keycols > 1 ? ",linkkeyid2 " : "") + (keycols > 2 ? ",linkkeyid3 " : "") + "FROM rsetitems, storageunit WHERE rsetitems.rsetid = ?  AND  rsetitems.keyid1 = storageunit.storageunitid ORDER BY 1", new Object[]{rsetid});
                    outputSDIList.addSDIList(data.getColumnValues("keyid1", ";"), keycols > 1 ? data.getColumnValues("keyid2", ";") : "", keycols > 2 ? data.getColumnValues("keyid3", ";") : "");
                } else {
                    if (!outputsdcid.equals("StorageUnitSDC")) throw new SapphireException("StorageUnit conversion must have StorageUnitSDC as either the input or output SDC");
                    int keycols = Integer.parseInt(sdcProcessor.getProperty(inputsdcid, "keycolumns"));
                    DataSet data = queryProcessor.getPreparedSqlDataSet("SELECT DISTINCT storageunit.storageunitid FROM rsetitems, storageunit WHERE rsetitems.rsetid = ? AND  rsetitems.sdcid = storageunit.linksdcid AND  rsetitems.keyid1 = storageunit.linkkeyid1 " + (keycols > 1 ? "AND rsetitems.keyid2 = storageunit.linkkeyid2 " : "") + (keycols > 2 ? "AND rsetitems.keyid3 = storageunit.linkkeyid3 " : "") + "ORDER BY 1", new Object[]{rsetid});
                    outputSDIList.addSDIList(data.getColumnValues("storageunitid", ";"));
                }
            }
            if (rsetid == null) return;
            damProcessor.clearRSet(rsetid);
        }
        catch (Throwable throwable) {
            if (rsetid == null) throw throwable;
            damProcessor.clearRSet(rsetid);
            throw throwable;
        }
    }
}

