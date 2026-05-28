/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletException
 */
package com.labvantage.sapphire.servlet.command;

import java.util.HashMap;
import javax.servlet.ServletException;

public class LoginException
extends ServletException {
    protected String nexturl = "";
    protected String username = "";
    protected String database = "";
    protected String message = "";
    protected boolean ignoreLogonUrl = false;
    protected HashMap nexturlParams;

    public LoginException(String nexturl) {
        this.nexturl = nexturl;
    }

    public LoginException(String nexturl, String message) {
        this(nexturl);
        this.message = message;
    }

    public LoginException(String nexturl, HashMap nexturlParams, String message, boolean ignoreLogonUrl) {
        this(nexturl);
        this.nexturlParams = nexturlParams;
        this.message = message;
        this.ignoreLogonUrl = ignoreLogonUrl;
    }

    public LoginException(String msg, String nexturl, String username, String database) {
        super(msg);
        this.nexturl = nexturl;
        this.username = username;
        this.database = database;
    }

    public String getUsername() {
        return this.username;
    }

    public String getDatabase() {
        return this.database;
    }

    public String getNextURL() {
        return this.nexturl;
    }

    public String getMessage() {
        String errorMessage = super.getMessage();
        if (errorMessage == null) {
            errorMessage = this.message;
        }
        return errorMessage;
    }

    public boolean ignoreLogonUrl() {
        return this.ignoreLogonUrl;
    }

    public HashMap getNexturlParams() {
        return this.nexturlParams;
    }
}

