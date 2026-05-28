/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.SapphireSaxHandler;
import java.io.BufferedReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.StringUtil;

public class SqlProcessor {
    public static void processSQL(DBAccess database, HashMap params, BufferedReader sqlReader, SapphireSaxHandler handler) throws SapphireException {
        try {
            Connection connection = database.getConnection();
            Statement stmt = null;
            CallableStatement callstmt = null;
            StringBuffer statement = new StringBuffer();
            String readline = "";
            boolean semicolonseparator = true;
            boolean inblock = false;
            boolean endofstatement = false;
            boolean callablestatement = false;
            boolean isPLB = false;
            while ((readline = sqlReader.readLine()) != null) {
                if (!readline.startsWith("--")) {
                    String param;
                    if (readline.contains("&&1")) {
                        param = (String)params.get("&&1");
                        if (param != null && param.length() > 0) {
                            readline = StringUtil.replaceAll(readline, "&&1", param);
                        } else {
                            throw new SapphireException("Param &&1 found in file but no &&1 parameter has been defined");
                        }
                    }
                    if (readline.contains("&&2")) {
                        param = (String)params.get("&&2");
                        if (param != null && param.length() > 0) {
                            readline = StringUtil.replaceAll(readline, "&&2", param);
                        } else {
                            throw new SapphireException("Param &&2 found in file but no &&2 parameter has been defined");
                        }
                    }
                    if (readline.indexOf(63) > -1) {
                        readline = StringUtil.replaceAll(readline, "?", "{escape '?'}");
                    }
                    if (readline.contains("CREATE OR REPLACE") && readline.contains("wrapped")) {
                        isPLB = true;
                    }
                    if (readline.contains("CREATE OR REPLACE PACKAGE") || readline.contains("CREATE OR REPLACE TYPE")) {
                        semicolonseparator = false;
                        statement.append(readline + "\n");
                    } else if (readline.regionMatches(true, 0, "create procedure", 0, 16)) {
                        semicolonseparator = false;
                        inblock = true;
                        statement.append(readline + "\n");
                    } else if (readline.indexOf(59) > -1) {
                        if (semicolonseparator) {
                            statement.append(readline.substring(0, readline.indexOf(59)));
                            endofstatement = true;
                        } else {
                            statement.append(readline + "\n");
                        }
                    } else if (readline.indexOf(47) > -1) {
                        if (readline.trim().equals("/")) {
                            endofstatement = true;
                        } else {
                            statement.append(readline + "\n");
                        }
                    } else if (readline.regionMatches(true, 0, "go", 0, 2)) {
                        if (readline.trim().toLowerCase().equals("go")) {
                            endofstatement = true;
                        } else {
                            statement.append(readline + "\n");
                        }
                    } else if (readline.trim().regionMatches(true, 0, "exec", 0, 4)) {
                        if (!inblock) {
                            statement.delete(0, statement.length());
                            if (database.isOracle()) {
                                statement.append("{call " + readline.substring(readline.indexOf("exec ") + 5) + "}");
                            } else {
                                statement.append(readline + "\n");
                            }
                            callablestatement = true;
                            endofstatement = true;
                        } else {
                            statement.append(readline + "\n");
                        }
                    } else {
                        statement.append(readline + "\n");
                        if (readline.regionMatches(true, 0, "begin", 0, 5) || readline.contains("BEGIN") || readline.contains("FUNCTION") || readline.contains("DECLARE")) {
                            inblock = true;
                            semicolonseparator = false;
                        }
                    }
                }
                if (!endofstatement) continue;
                if (statement.toString().trim().length() > 0) {
                    if (callablestatement) {
                        if (handler != null && !isPLB) {
                            handler.log("SQL>" + statement.toString().trim());
                        }
                        callstmt = connection.prepareCall(statement.toString().trim());
                        callstmt.execute();
                        callstmt.close();
                    } else {
                        if (handler != null && !isPLB) {
                            handler.log("SQL>" + statement.toString().trim());
                        }
                        stmt = connection.createStatement();
                        if (isPLB) {
                            stmt.execute(statement.toString());
                        } else {
                            stmt.execute(statement.toString().trim());
                        }
                        stmt.close();
                    }
                }
                statement.delete(0, statement.length());
                endofstatement = false;
                semicolonseparator = true;
                callablestatement = false;
                inblock = false;
                isPLB = false;
            }
            callstmt = null;
            stmt = null;
        }
        catch (SQLException sqle) {
            throw new SapphireException("SQLException: " + sqle.getMessage());
        }
        catch (Exception e) {
            throw new SapphireException("GeneralException: " + e.getMessage());
        }
    }
}

