/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.command;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.admin.command.Ant;
import com.labvantage.sapphire.admin.command.BuildEAR;
import com.labvantage.sapphire.admin.command.CheckDBPrivs;
import com.labvantage.sapphire.admin.command.CommandLine;
import com.labvantage.sapphire.admin.command.DBConnectionKey;
import com.labvantage.sapphire.admin.command.GenRAKFile;
import com.labvantage.sapphire.admin.command.InsertSysConfigDefaults;
import com.labvantage.sapphire.admin.command.PingServer;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.xml.ant.ImageToNVarcharmax;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Properties;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class SapphireCLI {
    private static final String[][] classNames = new String[][]{{"Ant", Ant.class.getName()}, {"BuildEAR", BuildEAR.class.getName()}, {"CheckDBPrivs", CheckDBPrivs.class.getName()}, {"DBConnectionKey", DBConnectionKey.class.getName()}, {"GenRAKFile", GenRAKFile.class.getName()}, {"InsertSysConfigDefaults", InsertSysConfigDefaults.class.getName()}, {"PingServer", PingServer.class.getName()}, {"ImageToNVarcharmax", ImageToNVarcharmax.class.getName()}};
    private static Properties defaults;
    private static final String version;

    public static void main(String[] args) {
        block11: {
            if (args.length >= 1) {
                try {
                    SapphireCLI.loadDefaults();
                    HashMap<Object, Object> commandParams = new HashMap<Object, Object>();
                    commandParams.putAll(SapphireCLI.getFileProps(args));
                    commandParams.put("SAPPHIRE_HOME", System.getProperty("sapphire.home"));
                    commandParams.put("JAVA_HOME", System.getProperty("java.home"));
                    commandParams.put("TOOLS_JAR", System.getProperty("tools.jar"));
                    for (int i = 1; i < args.length; ++i) {
                        if (!args[i].startsWith("-") || args[i].substring(1).equals("props") || i + 1 >= args.length) continue;
                        commandParams.put(args[i].substring(1), args[i + 1]);
                    }
                    boolean verbose = commandParams.get("verbose") != null && commandParams.get("verbose").equals("true");
                    String command = args[0];
                    if ("version".equalsIgnoreCase(command) || "build".equalsIgnoreCase(command)) {
                        System.out.println(version);
                        break block11;
                    }
                    if ("usage".equalsIgnoreCase(command) || "help".equalsIgnoreCase(command)) {
                        System.out.println(SapphireCLI.getUsage());
                        break block11;
                    }
                    boolean processed = false;
                    for (int i = 0; i < classNames.length && !processed; ++i) {
                        String className = classNames[i][0];
                        int len = className.length();
                        for (int j = 0; j < len && !processed; ++j) {
                            if (!className.substring(0, len - j).equalsIgnoreCase(command)) continue;
                            SapphireCLI.runCommand(classNames[i][1], commandParams, verbose);
                            processed = true;
                        }
                    }
                    if (processed) break block11;
                    String commandClass = command.startsWith("com.labvantage.sapphire.admin.command") ? command : "com.labvantage.sapphire.admin.command." + command;
                    try {
                        SapphireCLI.runCommand(commandClass, commandParams, verbose);
                    }
                    catch (Exception e) {
                        throw new SapphireException("Unrecognized command '" + command + "'!");
                    }
                }
                catch (Exception e) {
                    System.out.println(e.getMessage() + "\n");
                }
            } else {
                System.out.println(SapphireCLI.getUsage());
            }
        }
    }

    private static void runCommand(String className, HashMap commandParams, boolean verbose) throws Exception {
        Class<?> c = Class.forName(className);
        CommandLine commandClass = (CommandLine)c.newInstance();
        System.out.println("Processing command: " + commandClass.getCommandName() + "\n");
        try {
            long start = System.currentTimeMillis();
            commandClass.processCommand(commandParams, verbose);
            System.out.println("\nComplete (" + (System.currentTimeMillis() - start) / 1000L + " secs)");
        }
        catch (SapphireException e) {
            System.out.println(e.getMessage() + "\n");
            if (verbose) {
                e.printStackTrace();
            }
            System.out.println("Usage:\n  " + commandClass.getCommandUsage());
        }
    }

    private static String getUsage() {
        StringBuffer usage = new StringBuffer("Usage:\nsapphire [command] [-param1=value1 [-param2=value2 [-param3=value3] ...]]\n\nCommon parameters:\n  -props=[filename]   - specifies a properties/parameter file\n  -verbose            - adds additional output details of command progress\n  Parameters may be shortened providing they remain unique.\n\nCommands:\n  build | version\n   - Shows version/build");
        try {
            for (int i = 0; i < classNames.length; ++i) {
                Class<?> c = Class.forName(classNames[i][1]);
                CommandLine commandClass = (CommandLine)c.newInstance();
                if (!commandClass.isPublic()) continue;
                usage.append("\n  ").append(commandClass.getCommandName()).append("\n   - ").append(commandClass.getCommandDescription());
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return usage.toString();
    }

    static String getCommandParam(HashMap commandParams, String param, String errorMsg) throws SapphireException {
        int len = param.length();
        String value = "";
        for (int i = 0; i < len && ((value = (String)commandParams.get(param.substring(0, len - i))) == null || value.length() <= 0); ++i) {
        }
        if (!(value != null && value.length() != 0 || (value = SapphireCLI.getDefault(param)) != null && value.length() != 0 || errorMsg == null || errorMsg.length() <= 0)) {
            throw new SapphireException(errorMsg);
        }
        return value != null ? value : "";
    }

    static boolean existsCommandParam(HashMap commandParams, String param) {
        return commandParams.get(param) != null && ((String)commandParams.get(param)).length() > 0;
    }

    static String getDefault(String param) {
        return defaults.getProperty(param) != null ? defaults.getProperty(param) : "";
    }

    static void log(Object log) {
        System.out.println("  " + log);
    }

    public static DBUtil getDatabase(HashMap commandParams, boolean verbose) throws SapphireException {
        String sqldatabase = (String)commandParams.get("sqldatabase");
        String instancename = SapphireCLI.getCommandParam(commandParams, "instancename", "Instancename parameter (servername) not defined!");
        String servername = SapphireCLI.getCommandParam(commandParams, "servername", "Servername parameter (servername) not defined!");
        String port = SapphireCLI.getCommandParam(commandParams, "port", "Port parameter (servername) not defined!");
        String username = SapphireCLI.getCommandParam(commandParams, "username", "Username parameter (username) not defined!");
        String password = SapphireCLI.getCommandParam(commandParams, "password", "Password parameter (password) not defined!");
        DBUtil dbu = new DBUtil();
        if (sqldatabase != null && sqldatabase.length() > 0 && sqldatabase.indexOf("${") == -1) {
            if (verbose) {
                SapphireCLI.log("Servername : " + servername);
                SapphireCLI.log("Sqldatabase: " + sqldatabase);
                SapphireCLI.log("Username   : " + username);
                SapphireCLI.log("Password   : " + password);
            }
            dbu.setDatabase(instancename, servername, port, sqldatabase, username, password);
            SapphireCLI.log("Connected to database " + sqldatabase + " with user " + username + " on " + servername);
        } else {
            String sid = SapphireCLI.getCommandParam(commandParams, "sid", "SID parameter (sid) not defined!");
            if (verbose) {
                SapphireCLI.log("Servername: " + servername);
                SapphireCLI.log("Port      : " + port);
                SapphireCLI.log("SID       : " + sid);
                SapphireCLI.log("Username  : " + username);
                SapphireCLI.log("Password  : " + password);
            }
            dbu.setDatabase(servername, port, sid, username, password);
            SapphireCLI.log("Connected to database " + username + " on " + servername);
        }
        return dbu;
    }

    static Configuration createConfiguration(HashMap commandParams) throws SapphireException {
        String appserver = SapphireCLI.getCommandParam(commandParams, "applicationserver", "Application server parameter (applicationserver) not defined!");
        String hostname = SapphireCLI.getCommandParam(commandParams, "hostname", "");
        try {
            PropertyList configProps = new PropertyList();
            configProps.setProperty("serverinfo", appserver);
            configProps.setProperty("hostname", hostname.length() > 0 ? hostname : InetAddress.getLocalHost().getHostName());
            configProps.setProperty("LABVANTAGE_HOME", (String)commandParams.get("LABVANTAGE_HOME"));
            return Configuration.createInstance(configProps);
        }
        catch (UnknownHostException e) {
            throw new SapphireException("Failed to derive localhost name. Reason: " + e.getMessage(), e);
        }
    }

    private static Properties getFileProps(String[] args) throws Exception {
        String filename = null;
        for (int i = 1; i < args.length; ++i) {
            if (!args[i].equals("-props") || i + 1 >= args.length) continue;
            filename = args[i + 1];
            break;
        }
        Properties props = new Properties();
        if (filename != null && filename.length() > 0) {
            try {
                File propsFile = new File(filename);
                props.load(new FileInputStream(propsFile));
                props.setProperty("props", propsFile.getAbsolutePath());
            }
            catch (Exception e) {
                throw new Exception("Unable to load properties file from '" + filename + "'");
            }
        }
        return props;
    }

    private static void loadDefaults() throws Exception {
        File propsFile;
        String filename = System.getProperty("sapphire.home") + "/client/bin/defaults.props";
        defaults = new Properties();
        if (filename != null && filename.length() > 0 && (propsFile = new File(filename)).exists()) {
            try {
                defaults.load(new FileInputStream(propsFile));
            }
            catch (Exception e) {
                throw new Exception("Unable to load default properties file from '" + filename + "'");
            }
        }
    }

    public static String getInput() throws Exception {
        String inputstring = null;
        StringBuffer buf = new StringBuffer(80);
        int c = -1;
        boolean eof = false;
        try {
            block5: while (!eof) {
                c = System.in.read();
                switch (c) {
                    case -1: 
                    case 13: {
                        eof = true;
                        continue block5;
                    }
                }
                buf.append((char)c);
            }
            inputstring = buf.toString();
        }
        catch (Exception e) {
            throw new Exception("Failed to get input. Reason: " + e.getMessage(), e);
        }
        return inputstring;
    }

    static {
        version = "Sapphire version " + Build.getVersion() + ", build " + Build.getBuild();
    }
}

