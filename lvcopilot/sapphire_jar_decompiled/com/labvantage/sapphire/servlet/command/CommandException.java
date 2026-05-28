/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletException
 */
package com.labvantage.sapphire.servlet.command;

import javax.servlet.ServletException;

public class CommandException
extends ServletException {
    private String command = "";

    public CommandException(String msg, String command) {
        super(msg);
        this.command = command;
    }

    public String getCommand() {
        return this.command;
    }
}

