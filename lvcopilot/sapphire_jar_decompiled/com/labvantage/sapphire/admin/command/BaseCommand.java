/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.command;

import com.labvantage.sapphire.admin.command.BaseCLI;
import com.labvantage.sapphire.admin.command.CommandLine;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import sapphire.SapphireException;

public abstract class BaseCommand
implements CommandLine {
    private Properties defaults;
    private HashMap commandParams;
    private boolean verbose = false;

    protected void log(Object log) {
        System.out.println("  " + log);
    }

    protected void setDefaultDir() throws SapphireException {
        URL url = BaseCLI.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            String file = url.getFile();
            File classFile = new File(file);
            String dir = classFile.getParentFile().getAbsolutePath();
            this.setDefault("dir", dir);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to determine current directory. Reason: " + e.getMessage(), e);
        }
    }

    public void setDefaults(Properties defaults) {
        this.defaults = defaults;
    }

    protected String getDefault(String param) {
        return this.defaults.getProperty(param) != null ? this.defaults.getProperty(param) : "";
    }

    protected void setDefault(String param, String value) {
        this.defaults.setProperty(param, value);
    }

    public void setCommandParams(HashMap commandParams) {
        this.commandParams = commandParams;
    }

    public HashMap getCommandParams() {
        return this.commandParams;
    }

    protected String getCommandParam(String param, String errorMsg) throws SapphireException {
        int len = param.length();
        String value = "";
        for (int i = 0; i < len && ((value = (String)this.commandParams.get(param.substring(0, len - i))) == null || value.length() <= 0); ++i) {
        }
        if (!(value != null && value.length() != 0 || (value = this.getDefault(param)) != null && value.length() != 0 || errorMsg == null || errorMsg.length() <= 0)) {
            throw new SapphireException(errorMsg);
        }
        return value != null ? value : "";
    }

    protected boolean existsCommandParam(String param) {
        return this.commandParams.get(param) != null && ((String)this.commandParams.get(param)).length() > 0;
    }

    public boolean isVerbose() {
        return this.verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}

