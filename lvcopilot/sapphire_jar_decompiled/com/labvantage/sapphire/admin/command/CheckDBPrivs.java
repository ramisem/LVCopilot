/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.command;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.admin.command.CommandLine;
import com.labvantage.sapphire.admin.command.SapphireCLI;
import com.labvantage.sapphire.admin.install.Installer;
import java.util.HashMap;
import sapphire.SapphireException;

public class CheckDBPrivs
implements CommandLine {
    @Override
    public String getCommandName() {
        return "checkDBPrivs";
    }

    @Override
    public String getCommandDescription() {
        return "Checks that the correct database priviledges are defined";
    }

    @Override
    public String getCommandUsage() {
        return "checkdbprivs -servername=[servername] -port=[port] -sid=[sid] -username=[username] -password=[password]";
    }

    @Override
    public void processCommand(HashMap commandParams, boolean verbose) throws SapphireException {
        DBUtil dbu = SapphireCLI.getDatabase(commandParams, verbose);
        try {
            Installer.checkInstallPrivs(dbu.getConnection());
            SapphireCLI.log("Privileges OK.");
        }
        catch (SapphireException e) {
            SapphireCLI.log("Privileges NOT defined correctly!");
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

