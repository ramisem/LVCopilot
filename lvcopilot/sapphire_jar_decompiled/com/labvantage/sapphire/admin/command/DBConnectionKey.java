/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.command;

import com.labvantage.sapphire.admin.command.CommandLine;
import com.labvantage.sapphire.admin.command.SapphireCLI;
import com.labvantage.sapphire.admin.install.AdminDb;
import java.util.HashMap;
import sapphire.SapphireException;

public class DBConnectionKey
implements CommandLine {
    @Override
    public String getCommandName() {
        return "dbConnectionKey";
    }

    @Override
    public String getCommandDescription() {
        return "Generates a DBConnectionKey";
    }

    @Override
    public String getCommandUsage() {
        return "dbconnectionkey -username=[username] -password=[password]";
    }

    @Override
    public void processCommand(HashMap commandParams, boolean verbose) throws SapphireException {
        String username = SapphireCLI.getCommandParam(commandParams, "username", "Username parameter (username) not specified!");
        String password = SapphireCLI.getCommandParam(commandParams, "password", "Password parameter (password) not specified!");
        if (verbose) {
            SapphireCLI.log("Username: " + username);
            SapphireCLI.log("Password: " + password);
        }
        SapphireCLI.log("DBConnectionKey: " + AdminDb.getDBConnectionKey(username, password));
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

