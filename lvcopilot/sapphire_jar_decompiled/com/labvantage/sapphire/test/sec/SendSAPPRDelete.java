/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.test.sec.SAPWS.PRDeleteLineItem;
import com.labvantage.sapphire.test.sec.SAPWS.PRDeleteLineItem_OUTBindingStub;
import com.labvantage.sapphire.test.sec.SAPWS.PRDeleteLineItem_OUTServiceLocator;
import com.labvantage.sapphire.test.sec.SAPWS.Result;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class SendSAPPRDelete
extends BaseAction {
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String message = propertyList.getProperty("message");
        try {
            Trace.logInfo("::: Finding service locator");
            PRDeleteLineItem_OUTServiceLocator servicelocator = new PRDeleteLineItem_OUTServiceLocator();
            PRDeleteLineItem_OUTBindingStub binding = (PRDeleteLineItem_OUTBindingStub)servicelocator.getPRDeleteLineItem_OUTPort();
            Trace.logInfo("::: Setting username/passwd");
            binding.setUsername("SAPPHIRE");
            binding.setPassword("arteria");
            PRDeleteLineItem inputMessage = new PRDeleteLineItem(message);
            Trace.logInfo("Calling PRDeleteItem_OUT");
            Result result = binding.PRDeleteLineItem_OUT(inputMessage);
            Trace.logInfo("Got result::: " + result.getMessage());
            if (result.getMessage().equalsIgnoreCase("Error")) {
                Trace.logError("::: SAP returned error");
                throw new ActionException("SAP failed to delete PR, returned: " + result.getMessage());
            }
        }
        catch (Exception e) {
            Trace.logError("Send to SAP Failed");
            throw new ActionException("Failed to send PRDeleteItem to SAP: " + e.getMessage());
        }
    }
}

