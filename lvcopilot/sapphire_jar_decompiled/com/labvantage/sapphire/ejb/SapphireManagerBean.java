/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.EJBException
 *  javax.ejb.SessionBean
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.BaseManager;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.ejb.SapphireManagement;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.platform.SapphireDatabase;
import com.labvantage.sapphire.services.SapphireService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import java.util.ArrayList;
import java.util.HashMap;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import sapphire.xml.PropertyList;

public class SapphireManagerBean
extends BaseManager
implements SessionBean,
SapphireManagement {
    public SapphireManagerBean() {
        this.logName = "SapphireManager";
    }

    @Override
    public ArrayList<String> startup(HashMap startupProps) throws ManagerException {
        try {
            ArrayList<String> messages = SapphireService.startup(startupProps);
            if (SapphireService.isStarted()) {
                SapphireService.startAutomation();
            }
            return messages;
        }
        catch (Exception e) {
            this.logError("Failed to start LabVantage", e);
            throw new EJBException(e);
        }
    }

    @Override
    public ArrayList<String> startupDatabase(SapphireDatabase sapphireDatabase) throws ManagerException {
        try {
            return SapphireService.startupDatabase(sapphireDatabase);
        }
        catch (ServiceException e) {
            throw new EJBException((Exception)e);
        }
    }

    @Override
    public void shutdown() {
        try {
            SapphireService.stopAutomation();
            SapphireService.resetCache();
            ServiceLocator.clearInstance();
            Configuration.clearInstance();
        }
        catch (ServiceException e) {
            this.logError("Failed to stop automation", e);
            throw new EJBException((Exception)e);
        }
    }

    @Override
    public void restart() throws ManagerException {
        try {
            Configuration c = Configuration.getInstance();
            String sapphireHome = c.getSapphireHome();
            String serverInfo = c.getServerInfo();
            String hostname = c.getServerHostName();
            String applicationid = c.getApplicationid();
            String serverid = c.getServerid();
            boolean automationServer = c.isAutomationServer();
            PropertyList startupProps = new PropertyList();
            startupProps.setProperty("LABVANTAGE_HOME", sapphireHome);
            startupProps.setProperty("serverinfo", serverInfo);
            startupProps.setProperty("hostname", hostname);
            startupProps.setProperty("applicationid", applicationid);
            startupProps.setProperty("LABVANTAGE_SERVER", serverid);
            startupProps.setProperty("AUTOMATION_SERVER", "" + automationServer);
            this.shutdown();
            Configuration.createInstance(startupProps);
            this.startup(new HashMap());
        }
        catch (Exception e) {
            this.logError("Failed to restart LabVantage", e);
            throw new EJBException(e);
        }
    }

    @Override
    public void resetCache() throws ManagerException {
        try {
            SapphireService.resetCache();
        }
        catch (Exception e) {
            this.logError("Failed to reset caches", e);
            throw new EJBException(e);
        }
    }

    @Override
    public void resetSingleton() throws ManagerException {
    }

    @Override
    public void stopAutomation() throws ManagerException {
    }

    @Override
    public void startAutomation() throws ManagerException {
        try {
            SapphireService.startAutomation();
        }
        catch (ServiceException e) {
            this.logError("Failed to run LabVantageautomation", e);
            throw new EJBException((Exception)e);
        }
    }

    @Override
    public void startDatabaseAutomation(SapphireDatabase sapphireDatabase) throws ManagerException {
        try {
            SapphireService.startDatabaseAutomation(sapphireDatabase);
        }
        catch (ServiceException e) {
            this.logError("Failed to start automation in LabVantage database '" + sapphireDatabase + "'", e);
            throw new EJBException((Exception)e);
        }
    }

    @Override
    public void processCommands(ArrayList<HashMap<String, Object>> commands) throws ManagerException {
        if (commands != null) {
            for (HashMap<String, Object> command : commands) {
                if (command == null) continue;
                this.processCommand(command);
            }
        }
    }

    @Override
    public HashMap processCommand(HashMap<String, Object> commandProps) throws ManagerException {
        try {
            if (commandProps != null) {
                String command = (String)commandProps.get("command");
                if (command != null && command.length() > 0) {
                    if (!command.equalsIgnoreCase("restart")) {
                        return SapphireService.processCommand(commandProps);
                    }
                    this.restart();
                    return commandProps;
                }
                throw new ServiceException("No command specified");
            }
            throw new ServiceException("No command props provided");
        }
        catch (ServiceException e) {
            this.logError("Failed to process request", e);
            throw new EJBException((Exception)e);
        }
    }
}

