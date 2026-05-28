/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.workflow.gwt.server.steprequests;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.ConditionItem;
import com.labvantage.sapphire.modules.workflow.gwt.server.steprequests.BaseStepRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.SDIProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIList;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SplitterStep
extends BaseStepRequest {
    @Override
    public void executeRequest(CommandRequest commandRequest, CommandResponse commandResponse) throws SapphireException {
        String sdcid = commandRequest.getString("sdcid");
        SDIList inputSDIList = null;
        if (commandRequest.getString("inputvariabletype").equalsIgnoreCase("sdilist")) {
            inputSDIList = commandRequest.getSDIList("inputvariable");
        } else {
            inputSDIList = new SDIList();
            inputSDIList.setSdcid(sdcid);
            inputSDIList.addSDIList(commandRequest.getString("inputvariable"));
        }
        SDIList matchSDIList = new SDIList();
        matchSDIList.setSdcid(sdcid);
        SDIList nomatchSDIList = new SDIList();
        nomatchSDIList.setSdcid(sdcid);
        SDIProcessor sdiProcessor = new SDIProcessor(this.sapphireConnection.getConnectionId());
        SplitterStep.splitSDIList(sdiProcessor, sdcid, inputSDIList, matchSDIList, nomatchSDIList, commandRequest.getString("conditionsoperator", "AND").equals("AND"), commandRequest.getCollection("conditions"));
        if (commandRequest.getString("matchvariabletype").equalsIgnoreCase("sdilist")) {
            commandResponse.set("matchvariable", matchSDIList);
        } else {
            commandResponse.set("matchvariable", matchSDIList.getKeyid1());
        }
        if (commandRequest.getString("nomatchvariabletype").equalsIgnoreCase("sdilist")) {
            commandResponse.set("nomatchvariable", nomatchSDIList);
        } else {
            commandResponse.set("nomatchvariable", nomatchSDIList.getKeyid1());
        }
    }

    public static void splitSDIList(SDIProcessor sdiProcessor, String sdcid, SDIList inputSDIList, SDIList matchSDIList, SDIList nomatchSDIList, boolean andOperator, PropertyListCollection conditions) throws SapphireException {
        boolean orOperator = !andOperator;
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setRequestItem("primary");
        sdiRequest.setSDIList(inputSDIList);
        for (int i = 0; i < conditions.size(); ++i) {
            sdiRequest.setRequestItem(conditions.getPropertyList(i).getProperty("datasetname"));
            if (!conditions.getPropertyList(i).getProperty("conditiontype").equals("dm")) continue;
            sdiRequest.setRequestItem("dataitem");
        }
        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
        String[] keys = sdiData.getKeys("primary");
        HashMap<String, String> keyMap = new HashMap<String, String>();
        HashMap<String, Object> sdikeyMap = new HashMap<String, Object>();
        for (int i = 0; i < inputSDIList.size(); ++i) {
            boolean match = !orOperator;
            keyMap.clear();
            keyMap.put(keys[0], inputSDIList.getKeyid1(i));
            if (keys.length >= 2 && keys[1].length() > 0) {
                keyMap.put(keys[1], inputSDIList.getKeyid2(i));
            }
            if (keys.length >= 3 && keys[2].length() > 0) {
                keyMap.put(keys[2], inputSDIList.getKeyid3(i));
            }
            sdikeyMap.clear();
            sdikeyMap.put("sdcid", inputSDIList.getSdcid());
            sdikeyMap.put("keyid1", inputSDIList.getKeyid1(i));
            sdikeyMap.put("keyid2", inputSDIList.getKeyid2(i));
            sdikeyMap.put("keyid3", inputSDIList.getKeyid3(i));
            for (int j = 0; j < conditions.size(); ++j) {
                int findRow;
                PropertyList condition = conditions.getPropertyList(j);
                String conditiontype = condition.getProperty("conditiontype");
                String valueoperator = condition.getProperty("valueoperator");
                String existsoperator = condition.getProperty("existsoperator");
                String value1 = condition.getProperty("value1");
                String value2 = condition.getProperty("value2");
                String datasetname = conditiontype.equals("dm") ? "dataitem" : condition.getProperty("datasetname");
                DataSet dataset = sdiData.getDataset(datasetname);
                if (conditiontype.equals("vm")) {
                    if (dataset != null) {
                        findRow = dataset.findRow(datasetname.equalsIgnoreCase("sdispec") || datasetname.equalsIgnoreCase("dataset") || datasetname.equalsIgnoreCase("notes") || datasetname.equalsIgnoreCase("attachment") ? sdikeyMap : keyMap);
                        if (findRow <= -1) continue;
                        Object columnvalue = dataset.getObject(findRow, condition.getProperty("columnid"));
                        if (ConditionItem.conditionMatch(columnvalue, value1, value2, valueoperator)) {
                            if (!orOperator) continue;
                            match = true;
                            break;
                        }
                        if (!andOperator) continue;
                        match = false;
                        break;
                    }
                    throw new SapphireException("Datasetname '" + condition.getProperty("datasetname") + "' not found for " + sdcid + " items");
                }
                if (conditiontype.equals("dm")) {
                    if (dataset != null) {
                        Object columnvalue;
                        if (condition.getProperty("paramlistid").length() > 0) {
                            sdikeyMap.put("paramlistid", condition.getProperty("paramlistid"));
                        }
                        if (condition.getProperty("paramlistversionid").length() > 0) {
                            sdikeyMap.put("paramlistversionid", condition.getProperty("paramlistversionid"));
                        }
                        if (condition.getProperty("variantid").length() > 0) {
                            sdikeyMap.put("variantid", condition.getProperty("variantid"));
                        }
                        if (condition.getProperty("dataset").length() > 0) {
                            sdikeyMap.put("dataset", new BigDecimal(condition.getProperty("dataset")));
                        }
                        if (condition.getProperty("paramid").length() > 0) {
                            sdikeyMap.put("paramid", condition.getProperty("paramid"));
                        }
                        if (condition.getProperty("paramtype").length() > 0) {
                            sdikeyMap.put("paramtype", condition.getProperty("paramtype"));
                        }
                        if (condition.getProperty("replicateid").length() > 0) {
                            sdikeyMap.put("replicateid", new BigDecimal(condition.getProperty("replicateid")));
                        }
                        if ((findRow = dataset.findRow(sdikeyMap)) <= -1) continue;
                        String datatypes = dataset.getString(findRow, "datatypes");
                        boolean conditionMatch = false;
                        if (datatypes.equals("N") || datatypes.equals("NC")) {
                            columnvalue = dataset.getObject(findRow, "transformvalue");
                            conditionMatch = ConditionItem.conditionMatch(columnvalue, new BigDecimal(value1), value2.length() > 0 ? new BigDecimal(value2) : null, valueoperator);
                        } else if (datatypes.equals("D") || datatypes.equals("O") || datatypes.equals("DC") || datatypes.equals("OC")) {
                            columnvalue = dataset.getObject(findRow, "transformdt");
                            DateTimeUtil dtu = new DateTimeUtil();
                            conditionMatch = ConditionItem.conditionMatch(columnvalue, dtu.getCalendar(value1), value2.length() > 0 ? dtu.getCalendar(value2) : null, valueoperator);
                        } else {
                            columnvalue = dataset.getObject(findRow, "transformtext");
                            conditionMatch = ConditionItem.conditionMatch(columnvalue, value1, value2.length() > 0 ? value2 : null, valueoperator);
                        }
                        if (conditionMatch) {
                            if (!orOperator) continue;
                            match = true;
                            break;
                        }
                        if (!andOperator) continue;
                        match = false;
                        break;
                    }
                    throw new SapphireException("Datasetname '" + condition.getProperty("datasetname") + "' not found for " + sdcid + " items");
                }
                if (!conditiontype.equals("em")) continue;
                if (existsoperator.equals("any")) {
                    findRow = dataset.findRow(datasetname.equalsIgnoreCase("sdispec") || datasetname.equalsIgnoreCase("dataset") || datasetname.equalsIgnoreCase("notes") || datasetname.equalsIgnoreCase("attachment") ? sdikeyMap : keyMap);
                    if (findRow >= 0) {
                        if (!orOperator) continue;
                        match = true;
                        break;
                    }
                    if (!andOperator) continue;
                    match = false;
                    break;
                }
                int count = dataset.getFilteredDataSet(datasetname.equalsIgnoreCase("sdispec") || datasetname.equalsIgnoreCase("dataset") || datasetname.equalsIgnoreCase("notes") || datasetname.equalsIgnoreCase("attachment") ? sdikeyMap : keyMap).size();
                if (ConditionItem.conditionMatch(count, new Integer(value1), value2.length() > 0 ? new Integer(value2) : null, existsoperator)) {
                    if (!orOperator) continue;
                    match = true;
                    break;
                }
                if (!andOperator) continue;
                match = false;
                break;
            }
            if (match) {
                matchSDIList.addSDI(inputSDIList.getKeyid1(i), inputSDIList.getKeyid2(i), inputSDIList.getKeyid3(i));
                continue;
            }
            nomatchSDIList.addSDI(inputSDIList.getKeyid1(i), inputSDIList.getKeyid2(i), inputSDIList.getKeyid3(i));
        }
    }
}

