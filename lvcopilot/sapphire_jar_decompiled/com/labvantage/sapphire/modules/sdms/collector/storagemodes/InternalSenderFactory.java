/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.collector.storagemodes;

import com.labvantage.sapphire.modules.sdms.collector.SDMSCollector;
import com.labvantage.sapphire.modules.sdms.collector.collectortypes.BaseCollectorType;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.BaseFileSender;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.FileSenderFactory;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.SendDirectInternal;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.SendIndirect;

public class InternalSenderFactory
implements FileSenderFactory {
    private String connectionid;

    public InternalSenderFactory(String connectionid) {
        this.connectionid = connectionid;
    }

    @Override
    public BaseFileSender getInstance(SDMSCollector sdmsCollector, BaseCollectorType collectorType) {
        return collectorType.getSdmsCollector().isStorageModeDirect() ? new SendDirectInternal(sdmsCollector, collectorType, this.connectionid) : new SendIndirect(sdmsCollector, collectorType);
    }
}

