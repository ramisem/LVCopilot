/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.command;

import com.labvantage.sapphire.admin.command.CommandLine;
import com.labvantage.sapphire.util.ant.AntInstallListener;
import com.labvantage.sapphire.util.ant.AntUtil;
import java.io.File;
import java.util.HashMap;
import sapphire.SapphireException;

public class BuildEAR
implements CommandLine {
    @Override
    public String getCommandName() {
        return "buildEAR";
    }

    @Override
    public String getCommandDescription() {
        return "Builds a standard Sapphire EAR file";
    }

    @Override
    public String getCommandUsage() {
        return "buildear -props=[filename] [-buildfile=[filename]]";
    }

    @Override
    public void processCommand(HashMap commandParams, boolean verbose) throws SapphireException {
        File props;
        File file;
        String buildfile = (String)commandParams.get("buildfile");
        File file2 = file = buildfile != null && buildfile.length() > 0 ? new File(buildfile) : new File((String)commandParams.get("SAPPHIRE_HOME"), "client/bin/buildear.xml");
        if (!file.exists() || file.isDirectory()) {
            throw new SapphireException("Build file '" + file.getAbsolutePath() + "' not found!");
        }
        commandParams.put("project.props", commandParams.get("props") != null ? commandParams.get("props") : (String)commandParams.get("SAPPHIRE_HOME") + "/client/props/buildear.props");
        String target = (String)commandParams.get("target");
        if (target == null || target.length() == 0) {
            target = "buildear";
        }
        if ((props = new File((String)commandParams.get("project.props"))).exists()) {
            commandParams.putAll(AntUtil.resolvePropertyFile(props, commandParams));
        }
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

