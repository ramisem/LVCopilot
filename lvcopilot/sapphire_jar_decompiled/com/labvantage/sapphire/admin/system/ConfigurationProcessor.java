/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.system;

import com.labvantage.sapphire.BaseAccessor;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;

public class ConfigurationProcessor
extends BaseAccessor {
    public ConfigurationProcessor(String connectionid) {
        super(connectionid);
    }

    public ConfigurationProcessor(PageContext pageContext) {
        super(pageContext);
    }

    public String getConfigProperty(String propertyid) throws SapphireException {
        return this.getConfigProperty(propertyid, "");
    }

    public String getConfigProperty(String propertyid, String defaultvalue) throws SapphireException {
        try {
            return this.getConfigurationManager().getConfigProperty(this.getConnectionid(), propertyid, defaultvalue);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get config property '" + propertyid + "'", e);
        }
    }

    public String getSysConfigProperty(String propertyid) throws SapphireException {
        return this.getSysConfigProperty(propertyid, "");
    }

    public String getSysConfigProperty(String propertyid, String defaultvalue) throws SapphireException {
        try {
            if (this.getConnectionid().length() > 0) {
                return this.getConfigurationManager().getSysConfigProperty(this.getConnectionid(), propertyid, defaultvalue);
            }
            return "";
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get sysconfig property '" + propertyid + "'", e);
        }
    }

    public void setSysConfigProperty(String propertyid, String value) throws SapphireException {
        try {
            this.getConfigurationManager().setSysConfigProperty(this.getConnectionid(), propertyid, value);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get sysconfig property '" + propertyid + "'", e);
        }
    }

    public String getProfileProperty(String propertyid) throws SapphireException {
        return this.getProfileProperty("(system)", propertyid, "");
    }

    public String getProfileProperty(String sysuserid, String propertyid) throws SapphireException {
        return this.getProfileProperty(sysuserid, propertyid, "");
    }

    public String getProfileProperty(String sysuserid, String propertyid, String defaultvalue) throws SapphireException {
        try {
            if (this.getConnectionid().length() > 0) {
                return this.getConfigurationManager().getProfileProperty(this.getConnectionid(), sysuserid, propertyid, defaultvalue);
            }
            return "";
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get profile property '" + propertyid + "' for user '" + sysuserid + "'", e);
        }
    }

    public void setProfileProperty(String sysuserid, String propertyid, String value) throws SapphireException {
        try {
            this.getConfigurationManager().setProfileProperty(this.getConnectionid(), sysuserid, propertyid, value);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to set profile property '" + propertyid + "' for user '" + sysuserid + "' to '" + value + "'", e);
        }
    }
}

