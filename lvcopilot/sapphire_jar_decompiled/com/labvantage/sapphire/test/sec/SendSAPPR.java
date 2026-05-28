/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.test.sec.SAPWS.PRCreate;
import com.labvantage.sapphire.test.sec.SAPWS.PRCreate_OUTBindingStub;
import com.labvantage.sapphire.test.sec.SAPWS.PRCreate_OUTServiceLocator;
import com.labvantage.sapphire.test.sec.SAPWS.Result;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class SendSAPPR
extends BaseAction {
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String message = propertyList.getProperty("message");
        String indentId = propertyList.getProperty("indentid");
        try {
            Trace.logInfo("::: Finding service locator");
            PRCreate_OUTServiceLocator servicelocator = new PRCreate_OUTServiceLocator();
            PRCreate_OUTBindingStub binding = (PRCreate_OUTBindingStub)servicelocator.getPRCreate_OUTPort();
            Trace.logInfo("::: Setting username/passwd");
            binding.setUsername("SAPPHIRE");
            binding.setPassword("arteria");
            PRCreate inputMessage = new PRCreate(message);
            Trace.logInfo("Calling PRCreation_OUT");
            Result result = binding.PRCreate_OUT(inputMessage);
            Trace.logInfo("Got result::: " + result.getMessage());
            if (result.getMessage().equalsIgnoreCase("Error")) {
                Trace.logError("::: SAP returned error");
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "Indent");
                props.setProperty("keyid1", indentId);
                props.setProperty("status", "Error At SAP");
                this.getActionProcessor().processAction("EditSDI", "1", props, true);
                throw new ActionException("SAP failed to create PR, returned: " + result.getMessage());
            }
            Trace.logError("::: SAP returned Success");
            String prNumber = result.getMessage().substring(6);
            Trace.logError("Indent id being saved is: " + indentId);
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "Indent");
            props.setProperty("keyid1", indentId);
            props.setProperty("purchreqno", prNumber);
            props.setProperty("status", "SUCCESS");
            this.getActionProcessor().processAction("EditSDI", "1", props, true);
            Trace.logInfo("EditSDI called with prNumber ");
        }
        catch (Exception e) {
            Trace.logError("Send to SAP Failed");
            throw new ActionException("Failed to send PRCreate to SAP: " + e.getMessage());
        }
    }
}

