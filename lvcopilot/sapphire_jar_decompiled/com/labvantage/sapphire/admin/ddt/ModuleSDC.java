/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.platform.Configuration;
import java.util.Enumeration;
import java.util.Properties;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class ModuleSDC
extends BaseSDCRules {
    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.checkModules();
    }

    @Override
    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.checkModules();
    }

    private void checkModules() throws SapphireException {
        Properties moduleProps = Configuration.getInstance().getLicense(this.getDatabaseid()).getModuleProperties();
        Enumeration<?> e = moduleProps.propertyNames();
        while (e.hasMoreElements()) {
            String module = (String)e.nextElement();
            if ("U".equalsIgnoreCase(moduleProps.getProperty(module)) || "S".equalsIgnoreCase(moduleProps.getProperty(module)) || this.database.getPreparedCount("SELECT count(*) FROM modulesysuser, sysuser WHERE modulesysuser.sysuserid = sysuser.sysuserid AND ( sysuser.templateflag is null or sysuser.templateflag = 'N' ) AND ( sysuser.activeflag = 'Y' or sysuser.activeflag is null ) AND moduleid = ?", new Object[]{module}) <= Integer.parseInt(moduleProps.getProperty(module))) continue;
            throw new SapphireException("Module user assignment count exceeds license for module '" + module + "'");
        }
    }
}

