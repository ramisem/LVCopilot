/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.command;

import com.labvantage.sapphire.admin.command.CommandLine;
import com.labvantage.sapphire.admin.command.SapphireCLI;
import com.labvantage.sapphire.util.ant.AntInstallListener;
import com.labvantage.sapphire.util.ant.AntUtil;
import java.io.File;
import java.util.HashMap;
import sapphire.SapphireException;

public class Ant
implements CommandLine {
    @Override
    public String getCommandName() {
        return "ant";
    }

    @Override
    public String getCommandDescription() {
        return "Processes an ANT file";
    }

    @Override
    public String getCommandUsage() {
        return "ant -file=[filename] -target=[target]";
    }

    @Override
    public void processCommand(HashMap commandParams, boolean verbose) throws SapphireException {
        String filename = SapphireCLI.getCommandParam(commandParams, "file", "ANT build file parameter (file) not specified!");
        String target = SapphireCLI.getCommandParam(commandParams, "target", "Target parameter (target) not specified!");
        File file = new File(filename);
        if (!file.exists()) {
            throw new SapphireException("ANT build file '" + file.getAbsolutePath() + "' not found!");
        }
        SapphireCLI.log("Running ANT file: " + file.getAbsolutePath());
        AntUtil.runFile(0, file, null, target, commandParams, new AntInstallListener(verbose, verbose, false, verbose, true));
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

