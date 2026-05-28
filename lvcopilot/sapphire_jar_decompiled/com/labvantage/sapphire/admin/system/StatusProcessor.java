/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.system;

import com.labvantage.sapphire.BaseAccessor;
import java.util.List;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.util.DataSet;

public class StatusProcessor
extends BaseAccessor {
    public StatusProcessor(String connectionid) {
        super(connectionid);
    }

    public StatusProcessor(PageContext pageContext) {
        super(pageContext);
    }

    public DataSet getStatistics(int type) throws SapphireException {
        try {
            return this.getStatusManager().getStats(this.getConnectionid(), type);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get statistics", e);
        }
    }

    public void resetStatistics() throws SapphireException {
        try {
            this.getStatusManager().resetStats(this.getConnectionid());
        }
        catch (Exception e) {
            throw new SapphireException("Failed to reset statistics", e);
        }
    }

    public DataSet getCacheSizes(boolean includeContents) throws SapphireException {
        try {
            return this.getStatusManager().getCacheSizes(this.getConnectionid(), includeContents);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get cache sizes", e);
        }
    }

    public DataSet getClassLoaderStats(boolean includeContents) throws SapphireException {
        try {
            return this.getStatusManager().getClassLoaderStats(this.getConnectionid(), includeContents);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get class loaders", e);
        }
    }

    public List<String> getLSMExceptions() throws SapphireException {
        try {
            return this.getStatusManager().getLSMExceptions(this.getConnectionid());
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get lsm exceptions", e);
        }
    }

    public DataSet getTableSizes() throws SapphireException {
        try {
            return this.getStatusManager().getTableSizes(this.getConnectionid());
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get table sizes", e);
        }
    }

    public DataSet getMemoryStats() throws SapphireException {
        try {
            return this.getStatusManager().getMemoryStats(this.getConnectionid());
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get memory stats", e);
        }
    }

    public double getStatsMonitoringValue(String statsmonitorgroupid, String statsmonitoritemid) throws SapphireException {
        try {
            return this.getStatusManager().getStatsMonitoringValue(this.getConnectionid(), statsmonitorgroupid, statsmonitoritemid);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get stats monitoring value", e);
        }
    }
}

