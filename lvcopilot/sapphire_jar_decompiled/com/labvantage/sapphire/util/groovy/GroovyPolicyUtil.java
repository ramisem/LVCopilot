/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.util.groovy;

import com.labvantage.sapphire.Cache;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.xml.PropertyList;

public class GroovyPolicyUtil {
    private PageContext pageContext;
    private ConfigurationProcessor configurationProcessor;
    private static Cache policyCache = new Cache("Policy Node Cache", 1000);

    public GroovyPolicyUtil(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    public PropertyList getPolicy(String policyid, String nodeid) throws SapphireException {
        PropertyList nodePL = null;
        String cachekey = this.getConfigurationProcessor().getConnectionid() + ";" + policyid + ";" + nodeid;
        nodePL = (PropertyList)policyCache.get(cachekey);
        if (nodePL == null) {
            nodePL = this.getConfigurationProcessor().getPolicy(policyid, nodeid);
            policyCache.put(cachekey, nodePL);
        }
        return nodePL;
    }

    public PropertyList get(String policyid) throws Exception {
        if (policyid.contains("_")) {
            int pos = policyid.indexOf("_");
            return this.getPolicy(policyid.substring(0, pos), policyid.substring(pos + 1));
        }
        return this.getPolicy(policyid, "Sapphire Custom");
    }

    public ConfigurationProcessor getConfigurationProcessor() {
        if (this.configurationProcessor == null) {
            this.configurationProcessor = new ConfigurationProcessor(this.pageContext);
        }
        return this.configurationProcessor;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void resetCache() {
        Cache cache = policyCache;
        synchronized (cache) {
            policyCache.clear();
        }
    }
}

