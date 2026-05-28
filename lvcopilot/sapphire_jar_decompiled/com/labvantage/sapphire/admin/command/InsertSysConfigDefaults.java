/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.command;

import com.labvantage.sapphire.admin.command.CommandLine;
import com.labvantage.sapphire.admin.command.SapphireCLI;
import com.labvantage.sapphire.admin.install.AdminDb;
import java.util.HashMap;
import sapphire.SapphireException;

public class InsertSysConfigDefaults
implements CommandLine {
    @Override
    public String getCommandName() {
        return "insertSysConfigDefaults";
    }

    @Override
    public String getCommandDescription() {
        return "Inserts the default values into the sysconfig table";
    }

    @Override
    public String getCommandUsage() {
        return "insertsysconfigdefaults -servername=[servername] -port=[port] -sid=[sid] -username=[username] -password=[password]";
    }

    @Override
    public void processCommand(HashMap commandParams, boolean verbose) throws SapphireException {
        AdminDb.insertSysConfigDefaults(SapphireCLI.getDatabase(commandParams, verbose));
        SapphireCLI.log("Default values added to sysconfig.");
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

