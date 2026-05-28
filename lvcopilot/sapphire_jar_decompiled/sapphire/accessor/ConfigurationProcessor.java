/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.accessor;

import com.labvantage.sapphire.BaseAccessor;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.xml.PropertyList;

public class ConfigurationProcessor
extends BaseAccessor {
    private WebAdminProcessor wap;

    public ConfigurationProcessor(String connectionid) {
        super(connectionid);
        this.wap = new WebAdminProcessor(connectionid);
    }

    public ConfigurationProcessor(File rakFile, String connectionid) {
        super(rakFile, connectionid);
        this.wap = new WebAdminProcessor(rakFile, connectionid);
    }

    public ConfigurationProcessor(PageContext pageContext) {
        super(pageContext);
        this.wap = new WebAdminProcessor(pageContext);
    }

    public String getProfileProperty(String propertyid) {
        return this.getProfileProperty(propertyid, "");
    }

    public String getProfileProperty(String propertyid, String defaultValue) {
        try {
            String value = local ? this.getLocalAccessManager().getProfileProperty(this.getConnectionid(), this.getSapphireConnection().getSysuserId(), propertyid, defaultValue) : this.getRemoteAccessManager().getProfileProperty(this.getConnectionid(), this.getSapphireConnection().getSysuserId(), propertyid, defaultValue);
            return value;
        }
        catch (Exception e) {
            this.setError(this.parseServiceExceptionMsg(e, "Unable to get profile property (" + propertyid + ") - " + e.getMessage()), e);
            return "";
        }
    }

    public PropertyList getPolicy(String policyid) throws SapphireException {
        return this.getPolicy(policyid, "Default");
    }

    public PropertyList getPolicy(String policyid, String nodeid) throws SapphireException {
        return this.getPolicy(policyid, nodeid, true);
    }

    public PropertyList getPolicy(String policyid, String nodeid, boolean translate) throws SapphireException {
        PropertyList policyPropertyList = null;
        try {
            policyid = this.getInsteadOfPolicyId(policyid);
            SapphireConnection conn = this.getSapphireConnection();
            policyPropertyList = (PropertyList)CacheUtil.get(conn.getDatabaseId(), "PropertyTreeNode", policyid + ";" + nodeid);
            if (policyPropertyList == null) {
                PropertyTree policy = this.wap.getPropertyTree(policyid);
                try {
                    policyPropertyList = policy.getNodePropertyList(nodeid, true);
                }
                catch (SapphireException e) {
                    Trace.logWarn("Unable to load properties for policy " + policyid + " and node " + nodeid);
                    policyPropertyList = new PropertyList();
                }
                CacheUtil.put(conn.getDatabaseId(), "PropertyTreeNode", policyid + ";" + nodeid, policyPropertyList);
            }
            if (policyPropertyList != null) {
                String rolelist = conn.getRoleList();
                String modulelist = conn.getModuleList();
                Set<String> inactiveRoles = this.wap.getInactiveRoleList();
                String languageid = conn.getLanguage();
                policyPropertyList.setDbms(conn.getDbms());
                policyPropertyList.setDatabaseid(conn.getDatabaseId());
                policyPropertyList = translate ? policyPropertyList.copy(languageid == null || languageid.length() == 0 ? "(null)" : languageid, new TranslationProcessor(conn.getConnectionId()), rolelist, modulelist, inactiveRoles) : policyPropertyList.copy("(null)", new TranslationProcessor(conn.getConnectionId()), rolelist, modulelist, inactiveRoles);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (policyPropertyList == null) {
            Trace.logWarn("Unable to load properties for policy " + policyid + " and node " + nodeid);
            policyPropertyList = new PropertyList();
        }
        return policyPropertyList;
    }

    public PropertyList findPolicy(String policyid, String propertyid, String propertyvalue) {
        PropertyList policyPropertyList = null;
        try {
            ArrayList policyNodeList;
            policyid = this.getInsteadOfPolicyId(policyid);
            PropertyTree policy = this.wap.getPropertyTree(policyid);
            if (policy != null && (policyNodeList = policy.getAllNodes()) != null) {
                for (int i = 0; i < policyNodeList.size(); ++i) {
                    PropertyList nodePropertyList;
                    Node node = (Node)policyNodeList.get(i);
                    if (node.getNodeList().size() != 0 || !(nodePropertyList = policy.getNodePropertyList(node.getNodeId(), true)).getProperty(propertyid).equals(propertyvalue)) continue;
                    String rolelist = this.getSapphireConnection().getRoleList();
                    String modulelist = this.getSapphireConnection().getModuleList();
                    return nodePropertyList.copy(rolelist, modulelist);
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (policyPropertyList == null) {
            policyPropertyList = new PropertyList();
        }
        return policyPropertyList;
    }

    private SapphireConnection getSapphireConnection() {
        try {
            return local ? this.getLocalAccessManager().getSapphireConnection(this.getConnectionid()) : this.getRemoteAccessManager().getSapphireConnection(this.getConnectionid());
        }
        catch (Exception e) {
            this.setError(this.parseServiceExceptionMsg(e, "Unable to get connectionn details - " + e.getMessage()), e);
            return null;
        }
    }

    private String getInsteadOfPolicyId(String policyId) {
        String insteadOfPolicyId = null;
        try {
            insteadOfPolicyId = ConfigService.getConfigProperty("com.labvantage.sapphire.server.propertytree.insteadof." + policyId, "");
            if (insteadOfPolicyId != null && insteadOfPolicyId.trim().length() > 0) {
                Trace.logInfo("Substitute PropertyTree requested. Using " + insteadOfPolicyId + " instead of " + policyId + ".");
                return insteadOfPolicyId.trim();
            }
            return policyId;
        }
        catch (Throwable e) {
            Trace.logDebug("Warning: Error whilst trying to determine Substitute PropertyTree for " + policyId + ". No substitution will be made. Exception: " + e.getMessage());
            return policyId;
        }
    }
}

