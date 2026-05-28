/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.servlet.command;

import com.labvantage.sapphire.servlet.command.LoginException;

public class PasswordException
extends LoginException {
    private String password;
    private String errorCode = "GENERAL_ERROR";

    public PasswordException(String errorCode, String msg, String nexturl, String username, String password, String database) {
        super(msg, nexturl, username, database);
        this.password = password;
        this.errorCode = errorCode;
    }

    public String getPassword() {
        return this.password;
    }

    public String getErrorCode() {
        return this.errorCode;
    }
}

