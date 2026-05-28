/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.accessor;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.BaseAccessor;
import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.ejb.LocalAccessManagerLocal;
import com.labvantage.sapphire.ejb.RemoteAccessManager;
import com.labvantage.sapphire.util.StringHolder;
import java.io.File;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.util.SDIList;

public class DAMProcessor
extends BaseAccessor {
    public DAMProcessor(String connectionid) {
        super(connectionid);
    }

    public DAMProcessor(String nameserverlist, String connectionid) {
        super(connectionid);
    }

    public DAMProcessor(File rakFile, String connectionid) {
        super(rakFile, connectionid);
    }

    public DAMProcessor(PageContext pageContext) {
        super(pageContext);
    }

    public int clearRSet(String rsetid) {
        try {
            if (local) {
                this.getLocalAccessManager().clearRSet(this.getConnectionid(), new RSet(rsetid));
            } else {
                this.getRemoteAccessManager().clearRSet(this.getConnectionid(), new RSet(rsetid));
            }
            return 1;
        }
        catch (Exception e) {
            Trace.logError("ERROR", (Object)("Failed to clear RSet '" + rsetid + "'"), e);
            return 2;
        }
    }

    public void touchRSet(String rsetid) {
        try {
            if (local) {
                this.getLocalAccessManager().touchRSet(this.getConnectionid(), new RSet(rsetid));
            } else {
                this.getRemoteAccessManager().touchRSet(this.getConnectionid(), new RSet(rsetid));
            }
        }
        catch (Exception e) {
            Trace.logError("ERROR", (Object)("Failed to touch RSet '" + rsetid + "'"), e);
        }
    }

    public int createRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list, StringHolder rsetidholder) {
        try {
            RSet rset = local ? this.getLocalAccessManager().createRSet(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list) : this.getRemoteAccessManager().createRSet(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list);
            rsetidholder.value = rset.getRsetid();
            return 1;
        }
        catch (Exception e) {
            Trace.logError("ERROR", (Object)"Failed to create RSet", e);
            return 2;
        }
    }

    public String createRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list) throws SapphireException {
        try {
            RSet rset = local ? this.getLocalAccessManager().createRSet(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list) : this.getRemoteAccessManager().createRSet(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list);
            return rset.getRsetid();
        }
        catch (Exception e) {
            throw new SapphireException("Unable to create rset. Message: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionid())), e);
        }
    }

    public String createRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list, boolean viewHiddenRecord, int bypassSecurityCode) throws SapphireException {
        try {
            RSet rset = local ? this.getLocalAccessManager().createRSet(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, viewHiddenRecord, bypassSecurityCode) : this.getRemoteAccessManager().createRSet(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, viewHiddenRecord, bypassSecurityCode);
            return rset.getRsetid();
        }
        catch (Exception e) {
            throw new SapphireException("Unable to create rset. Message: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionid())), e);
        }
    }

    public int createRSetQ(String sdcid, String queryid, String param1, String param2, String param3, String param4, String param5, StringHolder rsetidholder) {
        try {
            RSet rset = local ? this.getLocalAccessManager().createRSetQ(this.getConnectionid(), sdcid, queryid, new String[]{param1, param2, param3, param4, param5}) : this.getRemoteAccessManager().createRSetQ(this.getConnectionid(), sdcid, queryid, new String[]{param1, param2, param3, param4, param5});
            rsetidholder.value = rset.getRsetid();
            return 1;
        }
        catch (Exception e) {
            Trace.logError("ERROR", (Object)"Failed to create RSet", e);
            return 2;
        }
    }

    public String createRSetQ(String sdcid, String queryid, String param1, String param2, String param3, String param4, String param5) throws SapphireException {
        try {
            RSet rset = local ? this.getLocalAccessManager().createRSetQ(this.getConnectionid(), sdcid, queryid, new String[]{param1, param2, param3, param4, param5}) : this.getRemoteAccessManager().createRSetQ(this.getConnectionid(), sdcid, queryid, new String[]{param1, param2, param3, param4, param5});
            return rset.getRsetid();
        }
        catch (Exception e) {
            throw new SapphireException("Unable to create rset. Message: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionid())), e);
        }
    }

    public String createRSetQ(String sdcid, String queryid, String[] params) throws SapphireException {
        try {
            RSet rset = local ? this.getLocalAccessManager().createRSetQ(this.getConnectionid(), sdcid, queryid, params) : this.getRemoteAccessManager().createRSetQ(this.getConnectionid(), sdcid, queryid, params);
            return rset.getRsetid();
        }
        catch (Exception e) {
            throw new SapphireException("Unable to create rset. Message: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionid())), e);
        }
    }

    public int createLockedRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list, StringHolder rsetidholder) {
        try {
            RSet rset;
            LocalAccessManagerLocal lam = null;
            RemoteAccessManager ram = null;
            if (local) {
                lam = this.getLocalAccessManager();
                rset = lam.createLockedRSet(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, "DA");
            } else {
                ram = this.getRemoteAccessManager();
                rset = ram.createLockedRSet(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, "DA");
            }
            rsetidholder.value = rset.getRsetid();
            if (rset.getPrimaryStatus() == 2) {
                if (local) {
                    lam.clearRSet(this.getConnectionid(), rset);
                } else {
                    ram.clearRSet(this.getConnectionid(), rset);
                }
                return 2;
            }
            return 1;
        }
        catch (Exception e) {
            Trace.logError("ERROR", (Object)"Failed to create locked RSet", e);
            return 2;
        }
    }

    public String createLockedRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list) throws SapphireException {
        try {
            RSet rset;
            LocalAccessManagerLocal lam = null;
            RemoteAccessManager ram = null;
            if (local) {
                lam = this.getLocalAccessManager();
                rset = lam.createLockedRSet(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, "DA");
            } else {
                ram = this.getRemoteAccessManager();
                rset = ram.createLockedRSet(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, "DA");
            }
            if (rset.getPrimaryStatus() == 2) {
                if (local) {
                    lam.clearRSet(this.getConnectionid(), rset);
                } else {
                    ram.clearRSet(this.getConnectionid(), rset);
                }
                throw new SapphireException("Unable to apply lock to rset.");
            }
            return rset.getRsetid();
        }
        catch (Exception e) {
            throw new SapphireException("Unable to create rset. Message: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionid())), e);
        }
    }

    public int createRSetDS(String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean propsMatch, StringHolder rsetidholder) {
        return this.createRSetDS(sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, propsMatch, false, false, rsetidholder);
    }

    public String createRSetDS(String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean propsMatch) throws SapphireException {
        return this.createRSetDS(sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, propsMatch, false, false);
    }

    public int createRSetDS(String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean propsMatch, boolean populateBoth, boolean calcexpand, StringHolder rsetidholder) {
        try {
            RSet rset = propsMatch ? (local ? this.getLocalAccessManager().createRSetDS(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, populateBoth, calcexpand) : this.getRemoteAccessManager().createRSetDS(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, populateBoth, calcexpand)) : (local ? this.getLocalAccessManager().createRSetDSNP(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, populateBoth, calcexpand) : this.getRemoteAccessManager().createRSetDSNP(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, populateBoth, calcexpand));
            rsetidholder.value = rset.getRsetid();
            return 1;
        }
        catch (Exception e) {
            Trace.logError("ERROR", (Object)"Failed to create locked RSet", e);
            return 2;
        }
    }

    public String createRSetDS(String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean propsMatch, boolean populateBoth, boolean calcexpand) throws SapphireException {
        try {
            RSet rset = propsMatch ? (local ? this.getLocalAccessManager().createRSetDS(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, populateBoth, calcexpand) : this.getRemoteAccessManager().createRSetDS(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, populateBoth, calcexpand)) : (local ? this.getLocalAccessManager().createRSetDSNP(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, populateBoth, calcexpand) : this.getRemoteAccessManager().createRSetDSNP(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, populateBoth, calcexpand));
            return rset.getRsetid();
        }
        catch (Exception e) {
            throw new SapphireException("Unable to create locked rset. Message: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionid())), e);
        }
    }

    public String getAllDSRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list) throws SapphireException {
        return this.createRSetDS(sdcid, keyid1list, keyid2list, keyid3list, "", "", "", "", false, true, false);
    }

    public String createRSetWI(String sdcid, String keyid1list, String keyid2list, String keyid3list, String workitemidlist, String workiteminstancelist, boolean populateBoth) throws SapphireException {
        try {
            RSet rset = local ? this.getLocalAccessManager().createRSetWI(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, workitemidlist, workiteminstancelist, populateBoth) : this.getRemoteAccessManager().createRSetWI(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, workitemidlist, workiteminstancelist, populateBoth);
            return rset.getRsetid();
        }
        catch (Exception e) {
            throw new SapphireException("Unable to create locked rset. Message: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionid())), e);
        }
    }

    public int createLockedRSetDS(String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean propsMatch, StringHolder rsetidholder) {
        try {
            LocalAccessManagerLocal lam = null;
            RemoteAccessManager ram = null;
            if (local) {
                lam = this.getLocalAccessManager();
            } else {
                ram = this.getRemoteAccessManager();
            }
            RSet rset = propsMatch ? (local ? lam.createLockedRSetDS(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, "DA", false) : ram.createLockedRSetDS(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, "DA", false)) : (local ? lam.createLockedRSetDSNP(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, "DA", false) : ram.createLockedRSetDSNP(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, "DA", false));
            rsetidholder.value = rset.getRsetid();
            if (rset.getDatasetStatus() == 2) {
                if (local) {
                    lam.clearRSet(this.getConnectionid(), rset);
                } else {
                    ram.clearRSet(this.getConnectionid(), rset);
                }
                return 2;
            }
            return 1;
        }
        catch (Exception e) {
            Trace.logError("ERROR", (Object)"Failed to create locked RSet DS", e);
            return 2;
        }
    }

    public String createLockedRSetDS(String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean propsMatch) throws SapphireException {
        try {
            RSet rset;
            LocalAccessManagerLocal lam = null;
            RemoteAccessManager ram = null;
            if (local) {
                lam = this.getLocalAccessManager();
            } else {
                ram = this.getRemoteAccessManager();
            }
            if (propsMatch) {
                rset = local ? lam.createLockedRSetDS(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, "DA", false) : ram.createLockedRSetDS(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, "DA", false);
            } else {
                RSet rSet = rset = local ? lam.createLockedRSetDSNP(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, "DA", false) : ram.createLockedRSetDSNP(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, "DA", false);
            }
            if (rset.getDatasetStatus() == 2) {
                if (local) {
                    lam.clearRSet(this.getConnectionid(), rset);
                } else {
                    ram.clearRSet(this.getConnectionid(), rset);
                }
                throw new SapphireException("Unable to apply lock to rset.");
            }
            return rset.getRsetid();
        }
        catch (Exception e) {
            throw new SapphireException("Unable to create locked rset. Message: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionid())), e);
        }
    }

    public int lockRSet(StringHolder rsetidholder) {
        try {
            RSet rset = local ? this.getLocalAccessManager().lockRSet(this.getConnectionid(), new RSet(rsetidholder.value), "DA", 3) : this.getRemoteAccessManager().lockRSet(this.getConnectionid(), new RSet(rsetidholder.value), "DA", 3);
            rsetidholder.value = rset.getRsetid();
            return 1;
        }
        catch (Exception e) {
            Trace.logError("ERROR", (Object)"Failed to lock RSet", e);
            return 2;
        }
    }

    public String lockRSet(String rsetid) throws SapphireException {
        try {
            RSet rset = local ? this.getLocalAccessManager().lockRSet(this.getConnectionid(), new RSet(rsetid), "DA", 3) : this.getRemoteAccessManager().lockRSet(this.getConnectionid(), new RSet(rsetid), "DA", 3);
            return rset.getRsetid();
        }
        catch (Exception e) {
            throw new SapphireException("Unable to lock rset. Message: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionid())), e);
        }
    }

    public SDIList checkSDIAccess(String sdcid, String keyid1list, String keyid2list, String keyid3list, boolean viewHiddenRecord, String operation) throws SapphireException {
        try {
            SDIList sdiList = local ? this.getLocalAccessManager().checkSDIAccess(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, viewHiddenRecord, operation) : this.getRemoteAccessManager().checkSDIAccess(this.getConnectionid(), sdcid, keyid1list, keyid2list, keyid3list, viewHiddenRecord, operation);
            return sdiList;
        }
        catch (Exception e) {
            throw new SapphireException("Unable to check SDI access. Message: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionid())), e);
        }
    }

    public boolean setGlobalLock(boolean lock) {
        try {
            return local ? this.getLocalAccessManager().setGlobalLock(this.getConnectionid(), lock) : this.getRemoteAccessManager().setGlobalLock(this.getConnectionid(), lock);
        }
        catch (Exception e) {
            Trace.logError("ERROR", (Object)"Failed to set global lock", e);
            return false;
        }
    }

    public boolean isGlobalLock() {
        try {
            return local ? this.getLocalAccessManager().isGlobalLock(this.getConnectionid()) : this.getRemoteAccessManager().isGlobalLock(this.getConnectionid());
        }
        catch (Exception e) {
            Trace.logError("ERROR", (Object)"Failed to get global lock", e);
            return false;
        }
    }
}

