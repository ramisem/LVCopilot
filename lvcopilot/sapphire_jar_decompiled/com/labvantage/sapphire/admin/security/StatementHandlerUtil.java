/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 */
package com.labvantage.sapphire.admin.security;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.ext.BaseStatementHandler;
import sapphire.xml.PropertyList;

public class StatementHandlerUtil {
    public static BaseStatementHandler getStatementHandler(String username, String database, String connectionid, String systemPassword, HttpServletRequest request, HttpServletResponse response) throws ServletException {
        BaseStatementHandler baseStatementHandler = null;
        PropertyList authenticationPL = new PropertyList();
        com.labvantage.sapphire.admin.system.ConfigurationProcessor configurationProcessor = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(connectionid);
        ConfigurationProcessor configProcessor = new ConfigurationProcessor(connectionid);
        authenticationPL.setDatabaseid(database);
        try {
            baseStatementHandler = (BaseStatementHandler)Class.forName("com.labvantage.sapphire.admin.security.LVDefaultStatementHandler").newInstance();
            baseStatementHandler.init(authenticationPL, username, database, connectionid, systemPassword);
            request.getSession().setAttribute("statementHandler", (Object)baseStatementHandler);
        }
        catch (Exception cnf) {
            throw new ServletException((Throwable)cnf);
        }
        return baseStatementHandler;
    }

    public static BaseStatementHandler getStatementHandler(String username, String database, String connectionid, HttpSession httpSession) throws ServletException {
        BaseStatementHandler baseStatementHandler = null;
        try {
            baseStatementHandler = (BaseStatementHandler)Class.forName("com.labvantage.sapphire.admin.security.BaseStatementHandler").newInstance();
        }
        catch (Exception cnf) {
            throw new ServletException((Throwable)cnf);
        }
        return baseStatementHandler;
    }
}

