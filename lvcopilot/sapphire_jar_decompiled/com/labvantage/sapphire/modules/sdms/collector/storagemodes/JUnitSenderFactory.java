/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.collector.storagemodes;

import com.labvantage.sapphire.modules.sdms.collector.SDMSCollector;
import com.labvantage.sapphire.modules.sdms.collector.collectortypes.BaseCollectorType;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.BaseFileSender;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.FileSenderFactory;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.JUnitSender;
import sapphire.SapphireException;

public class JUnitSenderFactory
implements FileSenderFactory {
    JUnitSender sender = null;

    @Override
    public BaseFileSender getInstance(SDMSCollector sdmsCollector, BaseCollectorType collectorType) {
        if (this.sender == null) {
            this.sender = new JUnitSender(sdmsCollector, collectorType);
        }
        return this.sender;
    }

    public JUnitSender getSender() throws SapphireException {
        if (this.sender == null) {
            return new JUnitSender(null, null);
        }
        return this.sender;
    }
}

