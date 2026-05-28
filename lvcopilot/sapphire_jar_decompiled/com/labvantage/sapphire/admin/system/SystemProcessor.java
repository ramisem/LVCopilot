/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.system;

import com.labvantage.sapphire.BaseAccessor;
import java.io.File;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;

public class SystemProcessor
extends BaseAccessor {
    public SystemProcessor(String connectionid) {
        super(connectionid);
    }

    public SystemProcessor(PageContext pageContext) {
        super(pageContext);
    }

    public SystemProcessor(File rakFile, String connectionid) {
        super(rakFile, connectionid);
    }

    public HashMap processCommand(HashMap commandParams) throws SapphireException {
        try {
            return local ? this.getLocalAccessManager().processCommand(commandParams) : this.getRemoteAccessManager().processCommand(commandParams);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get process command", e);
        }
    }
}

