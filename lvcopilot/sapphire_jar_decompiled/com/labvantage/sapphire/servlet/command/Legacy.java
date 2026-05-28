/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServletRequest
 */
package com.labvantage.sapphire.servlet.command;

import com.labvantage.sapphire.Trace;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class Legacy {
    public static String checkCommand(String command, HttpServletRequest request, ServletContext servletContext) {
        if (command == null || command.length() == 0) {
            String database = request.getParameter("database");
            String password = request.getParameter("password");
            String username = request.getParameter("username");
            String forcefile = request.getParameter("forcefile");
            String page = request.getParameter("page");
            String file = request.getParameter("file");
            String favorite = request.getParameter("favorite");
            String history = request.getParameter("history");
            String bulletin = request.getParameter("bulletin");
            String logoff = request.getParameter("logoff");
            String logMessage = "WARNING: No 'command' parameter found - check latest documentation on how use RequestController. ";
            if (database != null && database.length() > 0 && password != null && password.length() > 0 && username != null && username.length() > 0) {
                command = "login";
                if (request.getParameter("command") == null || request.getParameter("command").length() == 0) {
                    request.setAttribute("legacylogin", (Object)"Y");
                }
                Trace.log(logMessage + "Database, username and password param found - assumming login command.");
            } else if (logoff != null && logoff.equalsIgnoreCase("true")) {
                command = "logoff";
                Trace.log(logMessage + "logoff param found - assumming logoff command.");
            } else if (!(page == null || page.length() <= 0 || forcefile != null && forcefile.equals("Y") && file != null && file.length() != 0)) {
                command = "page";
                Trace.log(logMessage + "Page param found - assumming page command.");
            } else if (file != null && file.length() > 0) {
                command = "file";
                Trace.log(logMessage + "File param found - assumming file command.");
            } else if (favorite != null && favorite.length() > 0) {
                command = "favorite";
                Trace.log(logMessage + "Favorite param found - assumming favorite command.");
            } else if (history != null && history.length() > 0) {
                command = "history";
                Trace.log(logMessage + "History param found - assumming history command.");
            } else if (bulletin != null && bulletin.length() > 0) {
                command = "bulletin";
                Trace.log(logMessage + "Bulletin param found - assumming bulletin command.");
            }
        }
        return command;
    }

    public static String checkURL(String url) {
        if (url.startsWith("rc?")) {
            url = url.substring(3);
        }
        return url;
    }
}

