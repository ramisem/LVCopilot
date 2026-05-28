/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.server;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.eventplans.DeleteSDIEventPlan;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import java.util.Calendar;
import java.util.HashMap;
import org.json.JSONObject;
import org.json.JSONTokener;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDIEventPlanMaintPropertyHandler
extends PropertyHandler {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processProperties(HashMap props) throws SapphireException {
        block13: {
            String elementid = (String)props.get("__propertyhandler_elementid");
            String elementProps = (String)props.get("__" + elementid + "_properties");
            String sdcid = (String)props.get("sdcid");
            SDCProcessor sdcProcessor = new SDCProcessor(this.sapphireConnection.getConnectionId());
            int keycols = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns"));
            PropertyList keylist = (PropertyList)props.get("__keylist");
            try {
                String keyid3;
                SDIProcessor sdiProcessor = new SDIProcessor(this.sapphireConnection.getConnectionId());
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDIList(sdcid, keylist.getProperty("keyid1"), keylist.getProperty("keyid2"), keylist.getProperty("keyid3"));
                sdiRequest.setRequestItem("sdieventplan");
                sdiRequest.setRequestItem("sdieventplanitem");
                sdiRequest.setRequestItem("sdieventplanitemproperty");
                sdiRequest.setExtendedDataTypes(true);
                SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
                DataSet sdieventplan = sdiData.getDataset("sdieventplan");
                DataSet sdieventplanitem = sdiData.getDataset("sdieventplanitem");
                DataSet sdieventplanitemproperty = sdiData.getDataset("sdieventplanitemproperty");
                DataSetUtil.addUpdateColumn(sdieventplan);
                DataSetUtil.addUpdateColumn(sdieventplanitem);
                DataSetUtil.addUpdateColumn(sdieventplanitemproperty);
                String keyid1 = keylist.getProperty("keyid1");
                String keyid2 = keycols >= 2 ? keylist.getProperty("keyid2") : "(null)";
                String string = keyid3 = keycols >= 3 ? keylist.getProperty("keyid3") : "(null)";
                if (keyid1.length() <= 0) break block13;
                PropertyList saveData = new PropertyList(new JSONObject(new JSONTokener(elementProps)));
                PropertyListCollection saveEventPlans = saveData.getCollection("eventplans");
                HashMap<String, String> findMap = new HashMap<String, String>();
                StringBuffer delkeyid1 = new StringBuffer();
                StringBuffer delkeyid2 = new StringBuffer();
                StringBuffer delkeyid3 = new StringBuffer();
                StringBuffer deleventplanid = new StringBuffer();
                StringBuffer deleventplanversionid = new StringBuffer();
                StringBuffer deleventplansdcid = new StringBuffer();
                Calendar now = DateTimeUtil.getNowCalendar();
                for (int i = 0; i < saveEventPlans.size(); ++i) {
                    PropertyList saveEventPlan = saveEventPlans.getPropertyList(i);
                    String eventplanid = saveEventPlan.getProperty("eventplanid");
                    String eventplanversionid = saveEventPlan.getProperty("eventplanversionid");
                    String eventplansdcid = saveEventPlan.getProperty("eventplansdcid");
                    String scopeflag = saveEventPlan.getProperty("scopeflag");
                    int eventplaninstance = Integer.parseInt(saveEventPlan.getProperty("eventplaninstance", "1"));
                    findMap.clear();
                    findMap.put("eventplanid", eventplanid);
                    findMap.put("eventplanversionid", eventplanversionid);
                    findMap.put("eventplansdcid", eventplansdcid);
                    DataSet savesdieventplan = new DataSet(new JSONObject(new JSONTokener(saveEventPlan.getProperty("sdieventplan"))));
                    if (savesdieventplan.size() == 1) {
                        DataSet savesdieventplanproperties = new DataSet(new JSONObject(new JSONTokener(saveEventPlan.getProperty("sdieventplanitemproperty"))));
                        int row = sdieventplan.findRow(findMap);
                        if (row == -1) {
                            row = sdieventplan.addRow();
                            DataSetUtil.setInsert(sdieventplan, row);
                            sdieventplan.setValue(row, "sdcid", sdcid);
                            sdieventplan.setValue(row, "keyid1", keyid1);
                            sdieventplan.setValue(row, "keyid2", keyid2);
                            sdieventplan.setValue(row, "keyid3", keyid3);
                            sdieventplan.setValue(row, "eventplanid", eventplanid);
                            sdieventplan.setValue(row, "eventplanversionid", eventplanversionid);
                            sdieventplan.setValue(row, "eventplansdcid", eventplansdcid);
                            sdieventplan.setNumber(row, "eventplaninstance", 1);
                            sdieventplan.setValue(row, "createby", this.connectionInfo.getSysuserId());
                            sdieventplan.setDate(row, "createdt", now);
                            sdieventplan.setValue(row, "createtool", this.connectionInfo.getTool());
                            sdieventplan.setValue(row, "modby", this.connectionInfo.getSysuserId());
                            sdieventplan.setDate(row, "moddt", now);
                            sdieventplan.setValue(row, "modtool", this.connectionInfo.getTool());
                        } else {
                            DataSetUtil.setIgnore(sdieventplan, row);
                        }
                        if (savesdieventplanproperties.size() <= 0) continue;
                        for (int j = 0; j < savesdieventplanproperties.size(); ++j) {
                            String eventplanitemid = savesdieventplanproperties.getValue(j, "eventplanitemid");
                            String propertyid = savesdieventplanproperties.getValue(j, "propertyid");
                            findMap.put("eventplanitemid", eventplanitemid);
                            findMap.put("propertyid", propertyid);
                            row = sdieventplanitemproperty.findRow(findMap);
                            if (row == -1) {
                                findMap.remove("propertyid");
                                row = sdieventplanitem.findRow(findMap);
                                if (row == -1) {
                                    row = sdieventplanitem.addRow();
                                    DataSetUtil.setInsert(sdieventplanitem, row);
                                    sdieventplanitem.setValue(row, "sdcid", sdcid);
                                    sdieventplanitem.setValue(row, "keyid1", keyid1);
                                    sdieventplanitem.setValue(row, "keyid2", keyid2);
                                    sdieventplanitem.setValue(row, "keyid3", keyid3);
                                    sdieventplanitem.setValue(row, "eventplanid", eventplanid);
                                    sdieventplanitem.setValue(row, "eventplanversionid", eventplanversionid);
                                    sdieventplanitem.setValue(row, "eventplansdcid", eventplansdcid);
                                    sdieventplanitem.setNumber(row, "eventplaninstance", 1);
                                    sdieventplanitem.setValue(row, "eventplanitemid", eventplanitemid);
                                    sdieventplanitem.setValue(row, "createby", this.connectionInfo.getSysuserId());
                                    sdieventplanitem.setDate(row, "createdt", now);
                                    sdieventplanitem.setValue(row, "createtool", this.connectionInfo.getTool());
                                    sdieventplanitem.setValue(row, "modby", this.connectionInfo.getSysuserId());
                                    sdieventplanitem.setDate(row, "moddt", now);
                                    sdieventplanitem.setValue(row, "modtool", this.connectionInfo.getTool());
                                }
                                row = sdieventplanitemproperty.addRow();
                                DataSetUtil.setInsert(sdieventplanitemproperty, row);
                                sdieventplanitemproperty.setValue(row, "sdcid", sdcid);
                                sdieventplanitemproperty.setValue(row, "keyid1", keyid1);
                                sdieventplanitemproperty.setValue(row, "keyid2", keyid2);
                                sdieventplanitemproperty.setValue(row, "keyid3", keyid3);
                                sdieventplanitemproperty.setValue(row, "eventplanid", eventplanid);
                                sdieventplanitemproperty.setValue(row, "eventplanversionid", eventplanversionid);
                                sdieventplanitemproperty.setValue(row, "eventplansdcid", eventplansdcid);
                                sdieventplanitemproperty.setNumber(row, "eventplaninstance", 1);
                                sdieventplanitemproperty.setValue(row, "eventplanitemid", eventplanitemid);
                                sdieventplanitemproperty.setValue(row, "propertyid", propertyid);
                                sdieventplanitemproperty.setValue(row, "propertyvalue", savesdieventplanproperties.getValue(j, "propertyvalue"));
                                sdieventplanitemproperty.setValue(row, "mandatoryflag", savesdieventplanproperties.getValue(j, "mandatoryflag"));
                                sdieventplanitemproperty.setValue(row, "readonlyflag", savesdieventplanproperties.getValue(j, "readonlyflag"));
                                sdieventplanitemproperty.setValue(row, "hiddenflag", savesdieventplanproperties.getValue(j, "hiddenflag"));
                                sdieventplanitemproperty.setValue(row, "createby", this.connectionInfo.getSysuserId());
                                sdieventplanitemproperty.setDate(row, "createdt", now);
                                sdieventplanitemproperty.setValue(row, "createtool", this.connectionInfo.getTool());
                                sdieventplanitemproperty.setValue(row, "modby", this.connectionInfo.getSysuserId());
                                sdieventplanitemproperty.setDate(row, "moddt", now);
                                sdieventplanitemproperty.setValue(row, "modtool", this.connectionInfo.getTool());
                                continue;
                            }
                            DataSetUtil.setUpdate(sdieventplanitemproperty, row);
                            sdieventplanitemproperty.setValue(row, "propertyvalue", savesdieventplanproperties.getValue(j, "propertyvalue"));
                            sdieventplanitemproperty.setValue(row, "mandatoryflag", savesdieventplanproperties.getValue(j, "mandatoryflag"));
                            sdieventplanitemproperty.setValue(row, "readonlyflag", savesdieventplanproperties.getValue(j, "readonlyflag"));
                            sdieventplanitemproperty.setValue(row, "hiddenflag", savesdieventplanproperties.getValue(j, "hiddenflag"));
                            sdieventplanitemproperty.setValue(row, "modby", this.connectionInfo.getSysuserId());
                            sdieventplanitemproperty.setDate(row, "moddt", now);
                            sdieventplanitemproperty.setValue(row, "modtool", this.connectionInfo.getTool());
                        }
                        continue;
                    }
                    if (scopeflag.equals("G")) continue;
                    delkeyid1.append(";").append(keyid1);
                    delkeyid2.append(";").append(keyid2);
                    delkeyid3.append(";").append(keyid3);
                    deleventplanid.append(";").append(eventplanid);
                    deleventplanversionid.append(";").append(eventplanversionid);
                    deleventplansdcid.append(";").append(eventplansdcid);
                }
                if (delkeyid1.length() > 0) {
                    ActionProcessor ap = new ActionProcessor(this.sapphireConnection.getConnectionId());
                    PropertyList delProps = new PropertyList();
                    delProps.setProperty("sdcid", sdcid);
                    delProps.setProperty("keyid1", delkeyid1.substring(1));
                    delProps.setProperty("keyid2", delkeyid2.substring(1));
                    delProps.setProperty("keyid3", delkeyid3.substring(1));
                    delProps.setProperty("eventplanid", deleventplanid.substring(1));
                    delProps.setProperty("eventplanversionid", deleventplanversionid.substring(1));
                    delProps.setProperty("eventplansdcid", deleventplansdcid.substring(1));
                    ap.processActionClass(DeleteSDIEventPlan.class.getName(), delProps);
                }
                DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
                try {
                    dbu.setConnection(this.sapphireConnection);
                    DataSetUtil.insertUpdate(dbu, sdieventplan, "sdieventplan", new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "eventplanid", "eventplanversionid", "eventplansdcid", "eventplaninstance"});
                    DataSetUtil.insertUpdate(dbu, sdieventplanitem, "sdieventplanitem", new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "eventplanid", "eventplanversionid", "eventplansdcid", "eventplaninstance", "eventplanitemid"});
                    DataSetUtil.insertUpdate(dbu, sdieventplanitemproperty, "sdieventplanitemproperty", new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "eventplanid", "eventplanversionid", "eventplansdcid", "eventplaninstance", "eventplanitemid", "propertyid"});
                }
                finally {
                    dbu.reset();
                }
            }
            catch (Exception e) {
                throw new SapphireException("Failed to process SDIEventPlanMaint properties. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
            }
        }
    }
}

