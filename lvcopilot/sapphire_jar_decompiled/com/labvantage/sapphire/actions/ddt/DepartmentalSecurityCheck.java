/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.ddt;

import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.services.DataAccessService;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIList;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DepartmentalSecurityCheck
extends BaseAction {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String items = properties.getProperty("keyid1");
        String items2 = properties.getProperty("keyid2");
        String items3 = properties.getProperty("keyid3");
        if (items == null || items.length() <= 0) throw new SapphireException(this.getTranslationProcessor().translate("No sdis provided."));
        String operation = properties.getProperty("operation");
        if (operation == null || operation.length() <= 0) throw new SapphireException(this.getTranslationProcessor().translate("No operation provided."));
        String sdcid = properties.getProperty("sdcid");
        if (sdcid == null || sdcid.length() <= 0) throw new SapphireException(this.getTranslationProcessor().translate("No SDC Id provided."));
        String accesstype = properties.getProperty("accesstype");
        if (accesstype != null && accesstype.length() > 0 && accesstype.equalsIgnoreCase("world")) {
            properties.setProperty("operation", operation);
            properties.setProperty("failedsdis", "");
            properties.setProperty("passedsdis", items);
            return;
        } else {
            this.checkSDIAccessList(properties, operation, sdcid, items, items2, items3);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void checkSDIAccessList(PropertyList props, String operation, String sdcid, String items, String items2, String items3) throws SapphireException {
        StringBuffer faillist;
        ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(this.getConnectionId());
        boolean viewHidden = "Y".equals(configurationProcessor.getProfileProperty(this.connectionInfo.getSysuserId(), "viewhidden", "N"));
        SDIList sdiList = this.getDAMProcessor().checkSDIAccess(sdcid, items, items2, items3, viewHidden, operation);
        String retKeyId1 = "";
        int includedCnt = 0;
        String retKeyId2 = "";
        String retKeyId3 = "";
        try {
            includedCnt = sdiList.size();
            retKeyId1 = sdiList.getKeyid1();
            retKeyId2 = sdiList.getKeyid2();
            retKeyId3 = sdiList.getKeyid3();
            if ("SDIWorkItem".equals(sdcid) && "D".equals(this.getSDCProcessor().getProperty("WorkItem", "accesscontrolledflag"))) {
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("SDIWorkItem");
                sdiRequest.setKeyid1List(retKeyId1);
                sdiRequest.setRequestItem("primary");
                SDIData sdiworkitemSDIData = this.getSDIProcessor().getSDIData(sdiRequest);
                DataSet sdiworkitemDs = sdiworkitemSDIData.getDataset("primary");
                Logger.logInfo("Filter by WORKITEM departmental security");
                HashSet<String> set = new HashSet<String>();
                StringBuffer workitemids = new StringBuffer();
                StringBuffer workitemversionids = new StringBuffer();
                boolean hasCurrentVersion = false;
                for (int i = 0; i < sdiworkitemDs.getRowCount(); ++i) {
                    if (sdiworkitemDs.getValue(i, "workitemversionid").length() > 0) {
                        String idversion = sdiworkitemDs.getValue(i, "workitemid") + ";" + sdiworkitemDs.getValue(i, "workitemversionid");
                        if (set.contains(idversion)) continue;
                        set.add(idversion);
                        workitemids.append(";" + sdiworkitemDs.getValue(i, "workitemid"));
                        workitemversionids.append(";" + sdiworkitemDs.getValue(i, "workitemversionid"));
                        continue;
                    }
                    hasCurrentVersion = true;
                }
                String filteredkeyid1 = "";
                String filteredkeyid2 = "";
                if (workitemids.length() > 0) {
                    SDI workitemSDI = new SDI("WorkItem", workitemids.substring(1), workitemversionids.substring(1), null);
                    DataAccessService das = new DataAccessService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                    das.checkSDIAccess(workitemSDI, true, operation);
                    filteredkeyid1 = workitemSDI.getKeyid1();
                    filteredkeyid2 = workitemSDI.getKeyid2();
                }
                if (!(hasCurrentVersion || filteredkeyid1 != null && filteredkeyid1.length() != 0)) {
                    sdiworkitemDs.clear();
                } else if (workitemids.length() - 1 > filteredkeyid1.length()) {
                    String[] filteredworkitemids = StringUtil.split(filteredkeyid1, ";");
                    String[] filteredworkitemversionids = StringUtil.split(filteredkeyid2, ";");
                    set.clear();
                    for (int i = 0; i < filteredworkitemids.length; ++i) {
                        set.add(filteredworkitemids[i] + ";" + filteredworkitemversionids[i]);
                    }
                    ArrayList removeList = new ArrayList();
                    for (int i = 0; i < sdiworkitemDs.getRowCount(); ++i) {
                        if (sdiworkitemDs.getValue(i, "workitemversionid").length() <= 0 || set.contains(sdiworkitemDs.getValue(i, "workitemid") + ";" + sdiworkitemDs.getValue(i, "workitemversionid"))) continue;
                        removeList.add(sdiworkitemDs.get(i));
                    }
                    sdiworkitemDs.removeAll(removeList);
                }
                retKeyId1 = sdiworkitemDs.getColumnValues("sdiworkitemid", ";");
            }
        }
        catch (Exception e) {
            this.logger.info("Could not run procedure. Reason: " + e.getMessage());
        }
        finally {
            this.database.closeCall();
        }
        int keyCount = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
        DataSet data = new DataSet();
        DataSet includedData = new DataSet();
        data.addColumnValues("keyid1", 0, items, ";");
        if (keyCount > 1) {
            data.addColumnValues("keyid2", 0, items2, ";");
            if (keyCount > 2) {
                data.addColumnValues("keyid3", 0, items3, ";");
            }
        }
        if (includedCnt > 0) {
            includedData.addColumnValues("keyid1", 0, retKeyId1, ";");
            if (keyCount > 1) {
                includedData.addColumnValues("keyid2", 0, retKeyId2, ";");
                if (keyCount > 2) {
                    includedData.addColumnValues("keyid3", 0, retKeyId3, ";");
                }
            }
        }
        if (includedCnt > 0) {
            faillist = new StringBuffer();
            StringBuffer passlist = new StringBuffer();
            HashMap<String, String> filter = new HashMap<String, String>();
            for (int i = 0; i < data.getRowCount(); ++i) {
                filter.put("keyid1", data.getString(i, "keyid1", ""));
                if (keyCount > 1) {
                    filter.put("keyid2", data.getString(i, "keyid2", ""));
                    if (keyCount > 2) {
                        filter.put("keyid3", data.getString(i, "keyid3", ""));
                    }
                }
                if (includedData.findRow(filter) > -1) {
                    passlist.append("; ").append(data.getString(i, "keyid1", ""));
                    if (keyCount > 1) {
                        passlist.append("(").append(data.getString(i, "keyid2", ""));
                        if (keyCount > 2) {
                            passlist.append(",").append(data.getString(i, "keyid3", ""));
                        }
                        passlist.append(")");
                    }
                } else {
                    faillist.append("; ").append(data.getString(i, "keyid1", ""));
                    if (keyCount > 1) {
                        faillist.append("(").append(data.getString(i, "keyid2", ""));
                        if (keyCount > 2) {
                            faillist.append(",").append(data.getString(i, "keyid3", ""));
                        }
                        faillist.append(")");
                    }
                }
                filter.clear();
            }
            props.setProperty("operation", operation);
            props.setProperty("failedsdis", faillist.length() > 0 ? faillist.substring(1) : "");
            props.setProperty("passedsdis", passlist.length() > 0 ? passlist.substring(1) : "");
        } else {
            faillist = new StringBuffer();
            for (int i = 0; i < data.getRowCount(); ++i) {
                faillist.append("; ").append(data.getString(i, "keyid1", ""));
                if (keyCount <= 1) continue;
                faillist.append("(").append(data.getString(i, "keyid2", ""));
                if (keyCount > 2) {
                    faillist.append(",").append(data.getString(i, "keyid3", ""));
                }
                faillist.append(")");
            }
            props.setProperty("operation", operation);
            props.setProperty("failedsdis", faillist.length() > 0 ? faillist.substring(1) : "");
            props.setProperty("passedsdis", "");
        }
    }
}

