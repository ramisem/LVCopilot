/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.workflow.gwt.server.steprequests;

import com.labvantage.sapphire.modules.workflow.TaskDef;
import com.labvantage.sapphire.modules.workflow.WorkflowManager;
import com.labvantage.sapphire.modules.workflow.gwt.server.steprequests.BaseStepRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.util.SDIList;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.util.TaskContext;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ScanSelectorStep
extends BaseStepRequest {
    @Override
    public void executeRequest(CommandRequest commandRequest, CommandResponse commandResponse) throws SapphireException {
        String request = commandRequest.getString("request");
        if (request.equalsIgnoreCase("scanselectorgetchildparent")) {
            SDIList childVariable = commandRequest.getSDIList("childvariable");
            SafeSQL safeSQL = new SafeSQL();
            this.dbu.createPreparedResultSet("SELECT sourcesampleid, destsampleid FROM s_samplemap WHERE destsampleid IN (" + safeSQL.addIn(childVariable.getKeyid1(), ";") + ")", safeSQL.getValues());
            while (this.dbu.getNext()) {
                int childIndex = childVariable.getListIndex(this.dbu.getValue("destsampleid"), "", "");
                if (childIndex < 0) continue;
                childVariable.setSDIAttribute(childIndex, "parentid", this.dbu.getValue("sourcesampleid"));
            }
            commandResponse.set("childvariable", childVariable);
        } else if (request.equalsIgnoreCase("scanselectorvalidation")) {
            boolean valid = false;
            TaskContext taskContext = commandRequest.getTaskContext("taskcontext");
            String existCheck = commandRequest.getString("existcheck");
            String scanid = commandRequest.getString("scanid");
            if (existCheck.equals("eiq")) {
                String ioid = commandRequest.getString("ioid");
                SDIList sdiList = null;
                if (taskContext.isTestMode()) {
                    String inputkeyid1;
                    sdiList = new SDIList();
                    TaskDef taskDef = TaskDef.getInstance(this.sapphireConnection, taskContext.getTaskdefid(), taskContext.getTaskdefversionid(), taskContext.getTaskdefvariantid());
                    PropertyList taskdef = taskDef.getTaskdef();
                    PropertyListCollection taskio = taskdef.getCollection("taskio");
                    PropertyList io = taskio.find("ioid", ioid);
                    if (io != null && (inputkeyid1 = io.getProperty("exampletaskqueuekeyid1")).length() > 0) {
                        String connectortypeid = io.getProperty("connectortypeid");
                        this.dbu.createPreparedResultSet("SELECT connectortypesdcid FROM connectortype WHERE connectortypeid = ?", connectortypeid);
                        if (this.dbu.getNext()) {
                            sdiList.setSdcid(this.dbu.getValue("connectortypesdcid"));
                            sdiList.addSDIList(inputkeyid1, io.getProperty("exampletaskqueuekeyid2"), io.getProperty("exampletaskqueuekeyid3"));
                        }
                    }
                } else if (taskContext.isStandaloneMode()) {
                    sdiList = commandRequest.getSDIList("inputvariable");
                    if (sdiList == null) {
                        throw new SapphireException("Input queue variable not set");
                    }
                } else {
                    sdiList = WorkflowManager.getTaskQueueItems(this.sapphireConnection, this.dbu, taskContext.getTaskdefid(), taskContext.getTaskdefversionid(), taskContext.getTaskdefvariantid(), ioid, taskContext.getWorkflowexecid(), taskContext.getWorkflowdefid(), taskContext.getWorkflowdefversionid(), taskContext.getWorkflowdefvariantid(), taskContext.getTaskdefitemid(), taskContext.getTaskexecgroup(), -1, true, true, false, "W;Wevent;Wtimer;E;S;A;Aevent;Atimer");
                }
                int index = sdiList.getListIndex(scanid, "", "");
                valid = index != -1;
                commandResponse.set("keyid1", scanid);
                commandResponse.set("taskqueueid", sdiList.getSDIAttribute(index, "taskqueueid"));
                if (!valid && commandRequest.getBoolean("checkalias")) {
                    String sdcid = commandRequest.getString("sdcid");
                    String aliastype = commandRequest.getString("aliastype");
                    this.dbu.createPreparedResultSet("SELECT keyid1 FROM sdialias WHERE sdcid = ? AND keyid1 IN ('" + StringUtil.replaceAll(sdiList.getKeyid1(), ";", "','") + "') AND aliasid = ? " + (aliastype.length() > 0 ? " AND aliastype = '" + aliastype + "'" : ""), new Object[]{sdcid, scanid});
                    if (this.dbu.getNext()) {
                        commandResponse.set("keyid1", this.dbu.getValue("keyid1"));
                        commandResponse.set("taskqueueid", sdiList.getSDIAttribute(sdiList.getListIndex(scanid, "", ""), "taskqueueid"));
                        valid = true;
                    }
                }
            } else if (existCheck.equals("eid")) {
                String sdcid = commandRequest.getString("sdcid");
                SDCProcessor sdcProcessor = new SDCProcessor(this.sapphireConnection.getConnectionId());
                String keyid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
                this.dbu.createPreparedResultSet("SELECT " + keyid1 + " FROM " + sdcProcessor.getProperty(sdcid, "tableid") + " WHERE " + keyid1 + " = ?", new Object[]{scanid});
                if (this.dbu.getNext()) {
                    commandResponse.set("keyid1", this.dbu.getValue(keyid1));
                    valid = true;
                } else if (commandRequest.getBoolean("checkalias")) {
                    String aliastype = commandRequest.getString("aliastype");
                    this.dbu.createPreparedResultSet("SELECT keyid1 FROM sdialias WHERE sdcid = ? AND aliasid = ? " + (aliastype.length() > 0 ? " AND aliastype = '" + aliastype + "'" : ""), new Object[]{sdcid, scanid});
                    if (this.dbu.getNext()) {
                        commandResponse.set("keyid1", this.dbu.getValue("keyid1"));
                        valid = true;
                    }
                }
            }
            String parentid = commandRequest.getString("parentid");
            if (parentid.length() > 0) {
                this.dbu.createPreparedResultSet("SELECT sourcesampleid FROM s_samplemap WHERE destsampleid = ?", new Object[]{scanid});
                if (this.dbu.getNext()) {
                    valid = this.dbu.getValue("sourcesampleid").equals(parentid);
                }
            }
            commandResponse.set("valid", valid ? "Y" : "N");
        }
    }
}

