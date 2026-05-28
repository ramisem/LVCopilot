/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletRequest
 *  javax.servlet.ServletResponse
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.admin.security;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.ext.BaseStatementHandler;

public class LVDefaultStatementHandler
extends BaseStatementHandler {
    @Override
    public void renderPrompt(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.getRequestDispatcher("/statements.jsp").include((ServletRequest)request, (ServletResponse)response);
    }

    public void renderPrompt() throws Exception {
    }
}

