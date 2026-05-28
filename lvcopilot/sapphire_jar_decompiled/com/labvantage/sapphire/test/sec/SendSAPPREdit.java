/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.test.sec.SAPWS.PRUpdate;
import com.labvantage.sapphire.test.sec.SAPWS.PRUpdate_OUTBindingStub;
import com.labvantage.sapphire.test.sec.SAPWS.PRUpdate_OUTServiceLocator;
import com.labvantage.sapphire.test.sec.SAPWS.Result;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class SendSAPPREdit
extends BaseAction {
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String message = propertyList.getProperty("message");
        Trace.logInfo("Message being sent is: " + message);
        try {
            Trace.logInfo("::: Finding service locator");
            PRUpdate_OUTServiceLocator servicelocator = new PRUpdate_OUTServiceLocator();
            PRUpdate_OUTBindingStub binding = (PRUpdate_OUTBindingStub)servicelocator.getPRUpdate_OUTPort();
            Trace.logInfo("::: Setting username/passwd");
            binding.setUsername("SAPPHIRE");
            binding.setPassword("arteria");
            PRUpdate inputMessage = new PRUpdate(message);
            Trace.logInfo("Calling PRUpdate_OUT");
            Result result = binding.PRUpdate_OUT(inputMessage);
            Trace.logInfo("Got result::: " + result.getMessage());
            if (result.getMessage().equalsIgnoreCase("Error")) {
                Trace.logError("::: SAP returned error");
                throw new ActionException("SAP failed to update PR, returned: " + result.getMessage());
            }
        }
        catch (Exception e) {
            Trace.logError("Send to SAP Failed");
            throw new ActionException("Failed to send PRUpdate to SAP: " + e.getMessage());
        }
    }
}

