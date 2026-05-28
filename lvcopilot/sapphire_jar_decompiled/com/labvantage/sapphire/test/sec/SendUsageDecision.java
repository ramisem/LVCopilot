/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.test.sec.SAPWS.Result;
import com.labvantage.sapphire.test.sec.SAPWS.UsageDecision;
import com.labvantage.sapphire.test.sec.SAPWS.UsageDecision_OUTBindingStub;
import com.labvantage.sapphire.test.sec.SAPWS.UsageDecision_OUTServiceLocator;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class SendUsageDecision
extends BaseAction {
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String message = propertyList.getProperty("message");
        try {
            Trace.logInfo("::: Finding service locator");
            UsageDecision_OUTServiceLocator servicelocator = new UsageDecision_OUTServiceLocator();
            UsageDecision_OUTBindingStub binding = (UsageDecision_OUTBindingStub)servicelocator.getUsageDecision_OUTPort();
            Trace.logInfo("::: Setting username/passwd");
            binding.setUsername("SAPPHIRE");
            binding.setPassword("arteria");
            UsageDecision inputMessage = new UsageDecision(message);
            Trace.logInfo("Calling usageDecision_OUT");
            Result result = binding.usageDecision_OUT(inputMessage);
            Trace.logInfo("Got result::: " + result.getMessage());
            if (result.getMessage().equalsIgnoreCase("Error")) {
                Trace.logError("::: SAP returned error");
                throw new ActionException("SAP failed to record usage decision, returned: " + result.getMessage());
            }
        }
        catch (Exception e) {
            Trace.logError("Send to SAP Failed");
            throw new ActionException("Failed to send Usage Decision to SAP: " + e.getMessage());
        }
    }
}

