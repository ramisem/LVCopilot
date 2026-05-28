/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.command.diagnostics;

import com.labvantage.sapphire.admin.command.BaseCommand;
import com.labvantage.sapphire.admin.command.CommandLine;
import java.util.HashMap;
import sapphire.SapphireException;

public abstract class BaseDiagnosticsCommand
extends BaseCommand
implements CommandLine {
    @Override
    public void processCommand(HashMap commandParams, boolean verbose) throws SapphireException {
    }

    @Override
    public boolean isPublic() {
        return true;
    }
}

