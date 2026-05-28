/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.command;

import java.util.HashMap;
import sapphire.SapphireException;

public interface CommandLine {
    public String getCommandName();

    public String getCommandDescription();

    public String getCommandUsage();

    public void processCommand(HashMap var1, boolean var2) throws SapphireException;

    public void processCommand() throws SapphireException;

    public boolean isPublic();
}

