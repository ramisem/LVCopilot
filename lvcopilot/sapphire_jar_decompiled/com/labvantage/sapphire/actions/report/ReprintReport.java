/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.report;

import com.labvantage.sapphire.actions.report.BaseReportAction;
import com.labvantage.sapphire.report.SapphireReportEvent;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class ReprintReport
extends BaseReportAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 84009 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String reporteventid = properties.getProperty("reporteventid");
        SapphireReportEvent event = new SapphireReportEvent(reporteventid, "Report", (SapphireConnection)this.connectionInfo);
        event.redoEvent((SapphireConnection)this.connectionInfo);
    }
}

