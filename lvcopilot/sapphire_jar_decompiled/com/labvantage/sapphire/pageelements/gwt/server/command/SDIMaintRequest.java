/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.server.command;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.pageelements.gwt.server.command.BaseCommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import com.labvantage.sapphire.pageelements.gwt.server.command.SDIMaint;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.error.ErrorHandler;
import sapphire.util.SDIRequest;

public class SDIMaintRequest
extends BaseCommandRequest {
    @Override
    protected boolean processCommand(String command, CommandRequest commandRequest, CommandResponse commandResponse) throws SapphireException {
        if (command.equalsIgnoreCase("loadsdimaint")) {
            try {
                for (String name : commandRequest.keySet()) {
                    String changecontrolflag;
                    Object value = commandRequest.get(name);
                    if (!(value instanceof SDIRequest)) continue;
                    SDIRequest sdiRequest = (SDIRequest)value;
                    String sdcid = sdiRequest.getSDCid();
                    String keyid1 = sdiRequest.getKeyid1List();
                    if (keyid1.length() > 0 && sdcid.length() > 0 && ("Y".equals(changecontrolflag = CMTPolicy.getPolicy(this.getConnectionInfo().getConnectionId(), sdcid).getChangeControlledFlag()) || "T".equals(changecontrolflag))) {
                        sdiRequest.setValidateCheckout(true);
                        sdiRequest.setPrimaryLockOption("LA");
                    }
                    this.logInfo("Loading SDIMaint for " + new SDI(sdiRequest.getSDCid(), sdiRequest.getKeyid1List(), sdiRequest.getKeyid2List(), sdiRequest.getKeyid3List()).toString());
                    SDIMaint sdiMaint = new SDIMaint(this.getSDCProcessor().getPropertyList(sdiRequest.getSDCid()), this.getSDIProcessor().getSDIData(sdiRequest));
                    commandResponse.set(name, sdiMaint);
                }
                return true;
            }
            catch (Exception e) {
                throw new SapphireException("Failed to load SDIMaint data. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
            }
        }
        if (command.equalsIgnoreCase("savesdimaint")) {
            ActionService actionService = new ActionService(this.sapphireConnection);
            AuditService auditService = new AuditService(this.sapphireConnection);
            ErrorHandler errorHandler = new ErrorHandler();
            try {
                for (String name : commandRequest.keySet()) {
                    Object value = commandRequest.get(name);
                    if (!(value instanceof SDIMaint)) continue;
                    SDIMaint sdiMaint = (SDIMaint)value;
                    sdiMaint.setSDCProps(this.getSDCProcessor().getPropertyList(sdiMaint.getSdcid()));
                    this.logInfo("Saving SDIMaint for " + new SDI(sdiMaint.getSdcid(), sdiMaint.getKeyid1(), sdiMaint.getKeyid2(), sdiMaint.getKeyid3()).toString());
                    HashMap<String, String> extraProps = new HashMap<String, String>();
                    extraProps.put("auditreason", commandRequest.getString("auditreason", ""));
                    extraProps.put("auditactivity", commandRequest.getString("auditactivity", ""));
                    extraProps.put("auditsignedflag", commandRequest.getString("auditsignedflag", ""));
                    sdiMaint.save(actionService, auditService, errorHandler, this, extraProps);
                    commandResponse.set(name, sdiMaint);
                    commandResponse.set("sdcid", sdiMaint.getSdcid());
                }
                commandResponse.set("ERRORHANDLER", errorHandler);
                return true;
            }
            catch (ServiceException se) {
                commandResponse.set("ERRORHANDLER", errorHandler);
                return true;
            }
            catch (Exception e) {
                throw new SapphireException("Failed to save SDIMaint data. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
            }
        }
        return false;
    }
}

