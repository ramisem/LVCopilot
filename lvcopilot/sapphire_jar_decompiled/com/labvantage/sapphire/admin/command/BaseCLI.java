/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.command;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.admin.command.BaseCommand;
import com.labvantage.sapphire.admin.command.CommandLine;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;
import sapphire.SapphireException;

public class BaseCLI {
    private static final TreeMap<String, String> commands = new TreeMap();
    protected static final String version = "LabVantage version " + Build.getVersion() + ", build " + Build.getBuild();
    protected static Properties defaults;

    protected static void addCommand(String name, String className) {
        commands.put(name, className);
    }

    public static void main(String[] args) {
        block12: {
            if (args.length >= 1) {
                try {
                    BaseCLI.loadDefaults();
                    HashMap<Object, Object> commandParams = new HashMap<Object, Object>();
                    commandParams.putAll(BaseCLI.getFileProps(args));
                    commandParams.put("SAPPHIRE_HOME", System.getProperty("sapphire.home"));
                    commandParams.put("JAVA_HOME", System.getProperty("java.home"));
                    commandParams.put("TOOLS_JAR", System.getProperty("tools.jar"));
                    for (int i = 1; i < args.length; ++i) {
                        if (!args[i].startsWith("-") || args[i].substring(1).equals("props")) continue;
                        String arg = args[i];
                        int pos = arg.indexOf("=");
                        if (pos > 0) {
                            commandParams.put(arg.substring(1, pos), arg.substring(pos + 1));
                            continue;
                        }
                        if (i + 1 >= args.length) continue;
                        commandParams.put(arg.substring(1), args[i + 1]);
                    }
                    boolean verbose = commandParams.get("verbose") != null && commandParams.get("verbose").equals("true");
                    String command = args[0];
                    if ("version".equalsIgnoreCase(command) || "build".equalsIgnoreCase(command)) {
                        System.out.println(version);
                        break block12;
                    }
                    if ("usage".equalsIgnoreCase(command) || "help".equalsIgnoreCase(command)) {
                        System.out.println(BaseCLI.getUsage());
                        break block12;
                    }
                    boolean processed = false;
                    Iterator<String> iterator = commands.keySet().iterator();
                    while (iterator.hasNext() && !processed) {
                        String name = iterator.next();
                        int len = name.length();
                        for (int j = 0; j < len && !processed; ++j) {
                            if (!name.substring(0, len - j).equalsIgnoreCase(command)) continue;
                            BaseCLI.runCommand(commands.get(name), commandParams, verbose);
                            processed = true;
                        }
                    }
                    if (processed) break block12;
                    String commandClass = command.startsWith("com.labvantage.sapphire.admin.command") ? command : "com.labvantage.sapphire.admin.command." + command;
                    try {
                        BaseCLI.runCommand(commandClass, commandParams, verbose);
                    }
                    catch (Exception e) {
                        throw new SapphireException("Unrecognized command '" + command + "'!");
                    }
                }
                catch (Exception e) {
                    System.out.println(e.getMessage() + "\n");
                }
            } else {
                System.out.println(BaseCLI.getUsage());
            }
        }
    }

    private static void runCommand(String className, HashMap commandParams, boolean verbose) throws Exception {
        Class<?> c = Class.forName(className);
        BaseCommand commandClass = (BaseCommand)c.newInstance();
        commandClass.setDefaults(defaults);
        commandClass.setCommandParams(commandParams);
        commandClass.setVerbose(verbose);
        System.out.println("Processing command: " + commandClass.getCommandName() + "\n");
        try {
            long start = System.currentTimeMillis();
            commandClass.processCommand();
            System.out.println("\nComplete (" + (System.currentTimeMillis() - start) / 1000L + " secs)");
        }
        catch (Exception e) {
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
            for (String name : commands.keySet()) {
                Class<?> c = Class.forName(commands.get(name));
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

    private static void loadDefaults() throws Exception {
        defaults = new Properties();
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
}

