/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.command;

import com.labvantage.sapphire.RemoteAccessKey;
import com.labvantage.sapphire.admin.command.CommandLine;
import com.labvantage.sapphire.admin.command.SapphireCLI;
import com.labvantage.sapphire.platform.Configuration;
import java.io.File;
import java.util.HashMap;
import sapphire.SapphireException;

public class GenRAKFile
implements CommandLine {
    @Override
    public String getCommandName() {
        return "genRAKFile";
    }

    @Override
    public String getCommandDescription() {
        return "Generates a RAK (Remote Access Key) file";
    }

    @Override
    public String getCommandUsage() {
        return "genrakfile -rakfile=[rakfile] -applicationserver=[weblogic|websphere|jboss] -databaseid=[databaseid] -username=[username] -password=[password] [-hostname=[hostname]] [-serverurl=[serverurl]]";
    }

    @Override
    public void processCommand(HashMap commandParams, boolean verbose) throws SapphireException {
        String rakfile = SapphireCLI.getCommandParam(commandParams, "rakfile", "RAK filename parameter (rakfile) not defined!");
        String databaseid = SapphireCLI.getCommandParam(commandParams, "databaseid", "Databaseid parameter (databaseid) not defined!");
        String username = SapphireCLI.getCommandParam(commandParams, "username", "Username parameter (username) not defined!");
        String password = SapphireCLI.getCommandParam(commandParams, "password", "");
        String serverurl = SapphireCLI.getCommandParam(commandParams, "serverurl", "");
        Configuration configuration = SapphireCLI.createConfiguration(commandParams);
        File file = new File(rakfile);
        RemoteAccessKey.generateRemoteAccessKey(file, databaseid, serverurl.length() > 0 ? serverurl : configuration.getProviderURL(), username, password);
        SapphireCLI.log("RAK file '" + file.getAbsolutePath() + "' successfully generated.");
    }

    @Override
    public void processCommand() throws SapphireException {
        throw new SapphireException("Not supported in this version!");
    }

    @Override
    public boolean isPublic() {
        return true;
    }
}

