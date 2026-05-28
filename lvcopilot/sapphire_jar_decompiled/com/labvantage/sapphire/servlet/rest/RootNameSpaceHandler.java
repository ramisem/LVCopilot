/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.servlet.rest;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.servlet.rest.BaseNameSpaceHandler;
import com.labvantage.sapphire.servlet.rest.RestException;

public class RootNameSpaceHandler
extends BaseNameSpaceHandler {
    @Override
    public boolean requiresConnection() {
        return false;
    }

    @Override
    public void process() throws Exception {
        if (!this.doGet()) {
            throw new RestException(405, "Invalid request method", "Root resource only supports the GET method");
        }
        this.response.setHeader("Cache-Control", "max-age=3600");
        this.response.setDateHeader("Expires", System.currentTimeMillis() + 3600000L);
        this.setResponseValue("build", Build.getBuild());
        this.setResponseValue("version", Build.getVersion());
    }
}

