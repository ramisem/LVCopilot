/*
 * Decompiled with CFR 0.152.
 */
package sapphire.ext;

import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.FileSenderFactory;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public abstract class BaseCollectorType
extends com.labvantage.sapphire.modules.sdms.collector.collectortypes.BaseCollectorType
implements SDMSConstants {
    @Override
    public void configure(PropertyList collectorTypeProps) throws SapphireException {
    }

    protected void raiseInstrumentAlert(String alertType, String alertSeverity, String description, String message, boolean forceNew, boolean throwException) throws SapphireException {
        this.sdmsCollector.raiseInstrumentAlert(this.instrumentid, alertType, alertSeverity, description, message, forceNew);
        if (throwException) {
            throw new SapphireException(description + " - " + message);
        }
    }

    @Override
    public abstract int getCollectionPollInterval();

    @Override
    public abstract boolean isCollectionEnabled();

    @Override
    public abstract boolean isRunfileDeliveryEnabled();

    @Override
    public int getEmulatorPollInterval() {
        return 0;
    }

    @Override
    public boolean isContinuousOperation() {
        return false;
    }

    @Override
    public boolean isEmulatorEnabled() {
        return false;
    }

    @Override
    public abstract boolean doRunCollector(FileSenderFactory var1) throws Exception;

    @Override
    public abstract boolean doRunEmulator() throws Exception;

    @Override
    public String doDeliverRunFile(String filename, byte[] bytes) throws SapphireException {
        return "";
    }
}

