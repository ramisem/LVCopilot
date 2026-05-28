/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.issuemanagement.handlers;

import com.labvantage.sapphire.modules.issuemanagement.handlers.VCIssueHandler;

public class VCStgHandler
extends VCIssueHandler {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";

    public VCStgHandler() {
        this.setEndpointURL("http://vmvctest1us.lims.com/vcstg/services/SapphireWS?wsdl");
        this.setDatabaseId("vctest");
    }
}

