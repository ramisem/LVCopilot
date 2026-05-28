/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.test.sec.SAPWS.Result;
import com.labvantage.sapphire.test.sec.SAPWS.ResultRecord;
import com.labvantage.sapphire.test.sec.SAPWS.ResultRecord_OUTBindingStub;
import com.labvantage.sapphire.test.sec.SAPWS.ResultRecord_OUTServiceLocator;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class SendResultsRecord
extends BaseAction {
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String message = propertyList.getProperty("message");
        try {
            Trace.logInfo("::: Finding service locator");
            ResultRecord_OUTServiceLocator servicelocator = new ResultRecord_OUTServiceLocator();
            ResultRecord_OUTBindingStub binding = (ResultRecord_OUTBindingStub)servicelocator.getResultRecord_OUTPort();
            Trace.logInfo("::: Setting username/passwd");
            binding.setUsername("SAPPHIRE");
            binding.setPassword("arteria");
            ResultRecord inputMessage = new ResultRecord(message);
            Trace.logInfo("Calling resultRecord_OUT");
            Result result = binding.resultRecord_OUT(inputMessage);
            Trace.logInfo("Got result::: " + result.getMessage());
            if (result.getMessage().equalsIgnoreCase("Error")) {
                Trace.logError("::: SAP returned error");
                throw new ActionException("SAP failed to record results, returned: " + result.getMessage());
            }
        }
        catch (Exception e) {
            Trace.logError("Send to SAP Failed");
            throw new ActionException("Failed to send Recorded Results to SAP: " + e.getMessage());
        }
    }
}

