/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.accessor;

import com.labvantage.sapphire.BaseAccessor;
import com.labvantage.sapphire.Trace;
import java.io.File;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.ConnectionProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;

public class SDIProcessor
extends BaseAccessor {
    private ConnectionInfo connectionInfo;
    private M18NUtil m18n;

    public SDIProcessor(String connectionid) {
        super(connectionid);
        ConnectionProcessor conn = new ConnectionProcessor(connectionid);
        this.connectionInfo = conn.getConnectionInfo(connectionid);
        this.m18n = new M18NUtil(this.connectionInfo);
    }

    public SDIProcessor(String nameserverlist, String connectionid) {
        super(connectionid);
        ConnectionProcessor conn = new ConnectionProcessor(connectionid);
        this.connectionInfo = conn.getConnectionInfo(connectionid);
        this.m18n = new M18NUtil(this.connectionInfo);
    }

    public SDIProcessor(File rakFile, String connectionid) {
        super(rakFile, connectionid);
        ConnectionProcessor conn = new ConnectionProcessor(rakFile, connectionid);
        this.connectionInfo = conn.getConnectionInfo(connectionid);
        this.m18n = new M18NUtil(rakFile, this.connectionInfo);
    }

    public SDIProcessor(PageContext pageContext) {
        super(pageContext);
        ConnectionProcessor conn = new ConnectionProcessor(pageContext);
        this.connectionInfo = conn.getConnectionInfo(this.getConnectionid());
        this.m18n = new M18NUtil(null, this.connectionInfo);
    }

    public SDIData getSDIData(SDIRequest sdirequest) {
        try {
            SDIData sdiData;
            SDIData sDIData = sdiData = local ? this.getLocalAccessManager().getSDIData(this.getConnectionid(), sdirequest) : this.getRemoteAccessManager().getSDIData(this.getConnectionid(), sdirequest);
            if (this.connectionInfo != null) {
                Set datasets = sdiData.getDatasets();
                Iterator itr = datasets.iterator();
                while (itr.hasNext()) {
                    DataSet ds = sdiData.getDataset((String)itr.next());
                    if (this.m18n == null) continue;
                    ds.setM18NUtil(this.m18n);
                }
            }
            return sdiData;
        }
        catch (Exception e) {
            Trace.logError("ERROR", (Object)("Exception: " + e.getMessage()), e);
            this.setError("Error getting SDI data. Exception" + e.getMessage(), e);
            return null;
        }
    }

    public int getSDICount(SDIRequest sdiRequest) {
        return this.getSDICount(sdiRequest, true);
    }

    public int getSDICount(SDIRequest sdiRequest, boolean keepAlive) {
        try {
            return local ? this.getLocalAccessManager().getSDICount(this.getConnectionid(), sdiRequest, keepAlive) : this.getRemoteAccessManager().getSDICount(this.getConnectionid(), sdiRequest, keepAlive);
        }
        catch (Exception e) {
            Trace.logError("ERROR", (Object)("Exception: " + e.getMessage()), e);
            this.setError("Error getting SDI count. Exception" + e.getMessage(), e);
            return -1;
        }
    }
}

