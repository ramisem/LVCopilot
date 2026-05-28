/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workflow;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.actions.workflow.TaskInputEvent;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseAction;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;

public class CheckEventByPass
extends BaseAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String expression = properties.getProperty("expression");
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDIList(properties.getProperty("sdcid"), properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
        sdiRequest.setRequestItem("all");
        SDIProcessor sdiProcessor = this.getSDIProcessor();
        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
        if (sdiData != null) {
            this.logger.debug("Checking event bypass for " + new SDI(sdiRequest.getSDCid(), sdiRequest.getKeyid1List(), sdiRequest.getKeyid2List(), sdiRequest.getKeyid3List()) + " using expression: " + expression);
            try {
                StringBuffer keyid1 = new StringBuffer();
                StringBuffer keyid2 = new StringBuffer();
                StringBuffer keyid3 = new StringBuffer();
                String[] keycols = sdiData.getKeys("primary");
                HashMap bindMap = new HashMap();
                SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
                bindMap.put("user", new ConnectionInfo(sapphireConnection).getUserAttributeMap());
                StringBuffer log = new StringBuffer();
                ProcessingUtil.getSapphireObjectBindings(sapphireConnection, bindMap, (DBUtil)this.database, log, "CHECKEVENTBYPASS", true, true, true, true, true, true);
                expression = ProcessingUtil.insertHeaderCode(expression, false);
                DataSet primary = sdiData.getDataset("primary");
                for (int i = 0; i < primary.size(); ++i) {
                    bindMap.put("primary", primary.get(i));
                    String value = GroovyUtil.getInstance(this.connectionInfo).evaluateSecure(expression, bindMap);
                    if (!value.equalsIgnoreCase("Y") && !value.equalsIgnoreCase("true") && !value.equals("1")) continue;
                    keyid1.append(";").append(primary.getValue(i, keycols[0]));
                    keyid2.append(";").append(keycols[1].length() > 0 ? primary.getValue(i, keycols[1]) : "(null)");
                    keyid3.append(";").append(keycols[2].length() > 0 ? primary.getValue(i, keycols[2]) : "(null)");
                }
                if (keyid1.length() > 0) {
                    PropertyList actionProps = new PropertyList();
                    actionProps.setProperty("taskdefid", properties.getProperty("taskdefid"));
                    actionProps.setProperty("taskdefversionid", properties.getProperty("taskdefversionid"));
                    actionProps.setProperty("taskdefvariantid", properties.getProperty("taskdefvariantid"));
                    actionProps.setProperty("autoexec", properties.getProperty("autoexec"));
                    actionProps.setProperty("sdcid", properties.getProperty("sdcid"));
                    actionProps.setProperty("keyid1", keyid1.substring(1));
                    actionProps.setProperty("keyid2", keyid2.substring(1));
                    actionProps.setProperty("keyid3", keyid3.substring(1));
                    this.logger.debug("The following items met the event bypass conditions: " + actionProps.getProperty("keyid1") + ", " + actionProps.getProperty("keyid2") + ", " + actionProps.getProperty("keyid3"));
                    ActionProcessor actionProcessor = this.getActionProcessor();
                    actionProcessor.processActionClass(TaskInputEvent.class.getName(), actionProps);
                } else {
                    this.logger.debug("No items met the event bypass conditions");
                }
            }
            catch (Exception e) {
                throw new SapphireException("Failed to check event bypass expression: " + expression + ". Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
    }
}

