/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.command;

import com.labvantage.sapphire.RemoteAccessKey;
import com.labvantage.sapphire.admin.command.CommandLine;
import com.labvantage.sapphire.admin.command.SapphireCLI;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import java.io.File;
import java.util.HashMap;
import sapphire.SapphireException;

public class PingServer
implements CommandLine {
    @Override
    public String getCommandName() {
        return "pingServer";
    }

    @Override
    public String getCommandDescription() {
        return "Pings an application server using JNDI";
    }

    @Override
    public String getCommandUsage() {
        return "pingserver -rakfile=[rakfile]\n\n  Note: this command requires the client jars of the application server in the %SAPPHIRE_HOME%" + File.separator + "lib directory.";
    }

    @Override
    public void processCommand(HashMap commandParams, boolean verbose) throws SapphireException {
        String rakFile = SapphireCLI.getCommandParam(commandParams, "rakfile", "RAK filename parameter (rakfile) not specified!");
        File file = new File(rakFile);
        if (!file.exists()) {
            throw new SapphireException("RAK file '" + file.getAbsolutePath() + "' not found!");
        }
        try {
            ServiceLocator.getRemoteAccessManager(new RemoteAccessKey(file));
            SapphireCLI.log("Successfully pinged server.");
        }
        catch (Exception e) {
            throw new SapphireException("Failed to ping server!");
        }
    }

    @Override
    public void processCommand() throws SapphireException {
        throw new SapphireException("Not supported in this version!");
    }

    @Override
    public boolean isPublic() {
        return false;
    }
}

