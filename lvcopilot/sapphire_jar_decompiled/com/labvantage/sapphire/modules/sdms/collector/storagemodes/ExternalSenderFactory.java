/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.collector.storagemodes;

import com.labvantage.sapphire.modules.sdms.collector.SDMSCollector;
import com.labvantage.sapphire.modules.sdms.collector.collectortypes.BaseCollectorType;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.BaseFileSender;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.FileSenderFactory;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.SendDirectExternal;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.SendIndirect;

public class ExternalSenderFactory
implements FileSenderFactory {
    private boolean isIsolated;

    public ExternalSenderFactory(boolean isIsolated) {
        this.isIsolated = isIsolated;
    }

    @Override
    public BaseFileSender getInstance(SDMSCollector sdmsCollector, BaseCollectorType collectorType) {
        return this.isIsolated ? new SendIndirect(sdmsCollector, collectorType) : (sdmsCollector.isStorageModeDirect() ? new SendDirectExternal(sdmsCollector, collectorType) : new SendIndirect(sdmsCollector, collectorType));
    }
}

