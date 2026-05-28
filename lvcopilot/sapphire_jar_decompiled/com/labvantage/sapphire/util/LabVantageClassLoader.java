/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.util.LabVantageClassLoaderExecutable;
import com.labvantage.sapphire.util.LabVantageSecurityManager;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.file.FileManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONArray;
import sapphire.SapphireException;
import sapphire.accessor.SDIProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;

public class LabVantageClassLoader
extends URLClassLoader {
    private ClassLoaderType classLoaderType = null;
    private String classLoaderId = "";
    private ClassLoader system;
    private List<String> failed = new ArrayList<String>();
    private List<String> loadedClasses = new ArrayList<String>();
    private List<String> classPath = new ArrayList<String>();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void executeCode(LabVantageClassLoaderExecutable lamda, boolean newThread) throws SapphireException {
        block7: {
            ClassLoader defaultLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(this);
            try {
                if (newThread) {
                    Thread t = new Thread(() -> {
                        Trace.startThreadMDCBlank("LabVantageClassLoader");
                        try {
                            lamda.execute();
                        }
                        catch (SapphireException e) {
                            Trace.logError(e.getMessage(), e);
                        }
                        finally {
                            Trace.clearThreadMDC();
                        }
                    }, "LabVantageClassLoader_" + this.getType().typeName + "_" + this.classLoaderId);
                    t.setContextClassLoader(this);
                    t.start();
                    break block7;
                }
                Trace.setStartCodeBlock("LabVantageClassLoader.execute", this.getType().typeName + "_" + this.classLoaderId);
                try {
                    lamda.execute();
                }
                finally {
                    Trace.setEndCodeBlock("LabVantageClassLoader.execute");
                }
            }
            finally {
                Thread.currentThread().setContextClassLoader(defaultLoader);
            }
        }
    }

    public static Object instanitateClass(ClassLoader classLoader, String objectName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (classLoader != null && classLoader instanceof LabVantageClassLoader) {
            return ((LabVantageClassLoader)classLoader).instanitateClass(objectName);
        }
        try {
            return Class.forName(objectName).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (NoSuchMethodException e) {
            throw new InstantiationException("Failed to find constructor. " + e.getMessage());
        }
        catch (InvocationTargetException e) {
            throw new InstantiationException("Failed to invoke new instance. " + e.getMessage());
        }
    }

    public Object instanitateClass(String objectName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?> classToLoad = Class.forName(objectName, false, this);
        if (classToLoad.getClassLoader() instanceof LabVantageClassLoader) {
            Thread.currentThread().setContextClassLoader(this);
            try {
                Object obj = classToLoad.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                return obj;
            }
            catch (NoSuchMethodException e) {
                throw new InstantiationException("Failed to find constructor. " + e.getMessage());
            }
            catch (InvocationTargetException e) {
                throw new InstantiationException("Failed to invoke new instance. " + e.getMessage());
            }
            finally {
                Thread.currentThread().setContextClassLoader(classLoader);
            }
        }
        try {
            return Class.forName(objectName).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (NoSuchMethodException e) {
            throw new InstantiationException("Failed to find constructor. " + e.getMessage());
        }
        catch (InvocationTargetException e) {
            throw new InstantiationException("Failed to invoke new instance. " + e.getMessage());
        }
    }

    public static void executeCode(ClassLoaderType type, String classLoaderId, String appResourceId, ConnectionInfo connectionInfo, LabVantageClassLoaderExecutable lamda, boolean newThread) throws SapphireException {
        LabVantageClassLoader.executeCode(LabVantageClassLoader.getClassLoader(type, classLoaderId, appResourceId, null, connectionInfo), lamda, newThread);
    }

    public static void executeCode(ClassLoader classLoader, LabVantageClassLoaderExecutable lamda, boolean newThread) throws SapphireException {
        if (classLoader != null && classLoader instanceof LabVantageClassLoader) {
            ((LabVantageClassLoader)classLoader).executeCode(lamda, newThread);
        } else if (newThread) {
            Thread t = new Thread(() -> {
                try {
                    lamda.execute();
                }
                catch (SapphireException e) {
                    Trace.logError(e.getMessage(), e);
                }
            });
            t.start();
        } else {
            lamda.execute();
        }
    }

    public static void reset(String databaseid) {
        CacheUtil.clear(databaseid, "ClassLoaders", true);
    }

    public static void reset(ClassLoaderType type, String databaseid) {
        CacheUtil.removeAllStartWith(databaseid, "ClassLoaders", type.getTypeName(), true);
    }

    public static void reset(ClassLoaderType type, String classloaderid, String databaseid) {
        CacheUtil.remove(databaseid, "ClassLoaders", type.getTypeName() + "_" + classloaderid, true);
    }

    private void setType(ClassLoaderType type) {
        this.classLoaderType = type;
    }

    public ClassLoaderType getType() {
        return this.classLoaderType;
    }

    public static void resetAll(String databaseid) {
        LabVantageClassLoader.reset(null, databaseid);
    }

    private static void grabJars(Path p, List<URL> jars, String[] excludedJars) throws SapphireException {
        try {
            List subjars = Files.walk(p, new FileVisitOption[0]).filter(superPath -> FileManager.getExtension(superPath.toString()).equalsIgnoreCase("jar")).sorted().collect(Collectors.toList());
            if (subjars != null && subjars.size() > 0) {
                for (Path sp : subjars) {
                    String f = FileManager.getFileName(sp.toString(), false);
                    boolean found = false;
                    if (excludedJars != null) {
                        for (String f1 : excludedJars) {
                            String f2 = FileManager.getFileName(f1, false);
                            if (!f2.equalsIgnoreCase(f)) continue;
                            found = true;
                            break;
                        }
                    }
                    if (found) continue;
                    jars.add(sp.toUri().toURL());
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to use library path " + p.toString(), e);
        }
    }

    private static void loadAttachments(DataSet sdiattachment, String attachmentclass, AttachmentProcessor attachmentProcessor, List<URL> jars, String[] excludedJars) throws SapphireException {
        if (sdiattachment != null && sdiattachment.getRowCount() > 0) {
            for (int a = 0; a < sdiattachment.getRowCount(); ++a) {
                if (attachmentclass != null && attachmentclass.length() != 0 && !sdiattachment.getValue(a, "attachmentclass", "").equalsIgnoreCase(attachmentclass)) continue;
                try {
                    String sdcid = sdiattachment.getValue(a, "sdcid", "");
                    String keyid1 = sdiattachment.getValue(a, "keyid1", "(null)");
                    String keyid2 = sdiattachment.getValue(a, "keyid2", "(null)");
                    String keyid3 = sdiattachment.getValue(a, "keyid3", "(null)");
                    sapphire.attachment.Attachment jar = sapphire.attachment.Attachment.getAttachment(sdcid, keyid1, keyid2, keyid3, Integer.parseInt(sdiattachment.getValue(a, "attachmentnum", "")));
                    Path p = attachmentProcessor.getSDIAttachmentLocalFile((Attachment)jar, true);
                    if (p != null && Files.exists(p, new LinkOption[0])) {
                        if (Files.isDirectory(p, new LinkOption[0])) {
                            LabVantageClassLoader.grabJars(p, jars, excludedJars);
                            continue;
                        }
                        try {
                            jars.add(p.toUri().toURL());
                            continue;
                        }
                        catch (Exception e) {
                            throw new SapphireException("Failed to use library path. ", e);
                        }
                    }
                    throw new SapphireException("Failed to find library path. ");
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to load library path. ", e);
                }
            }
        }
    }

    private static String getDynamicClassPath(ConnectionInfo connectionInfo) {
        try {
            ConfigurationProcessor configuration = new ConfigurationProcessor(connectionInfo.getConnectionId());
            String dynamicclasslibary = configuration.getSysConfigProperty("dynamicclasslibrary", "[applicationhome]/dynamicclasslibraries/");
            if (dynamicclasslibary.length() > 0) {
                dynamicclasslibary = FileUtil.substituteConfigurationPaths(dynamicclasslibary);
                Trace.logDebug("dynamicclasslibary path: " + dynamicclasslibary);
            }
            return dynamicclasslibary;
        }
        catch (Exception e) {
            Trace.logWarn("Failed to get dynamic path", e);
            return "";
        }
    }

    private static boolean loadDynamicPath(Path dcl, List<URL> jars, String[] excludedJars) throws SapphireException {
        boolean loaded = false;
        if (Files.exists(dcl, new LinkOption[0]) && Files.isDirectory(dcl, new LinkOption[0])) {
            Path c_dcl = dcl.resolve("custom");
            if (c_dcl == null || !Files.exists(c_dcl, new LinkOption[0])) {
                c_dcl = dcl.resolve("Custom");
            }
            if (c_dcl != null && Files.exists(c_dcl, new LinkOption[0]) && Files.isDirectory(c_dcl, new LinkOption[0])) {
                LabVantageClassLoader.grabJars(c_dcl, jars, excludedJars);
                Path classes = c_dcl.resolve("classes");
                if (Files.exists(classes, new LinkOption[0]) && Files.isDirectory(classes, new LinkOption[0])) {
                    try {
                        jars.add(classes.toUri().toURL());
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                loaded = true;
                Trace.logDebug("Loaded custom library: " + c_dcl.toString());
            } else {
                Trace.logDebug("No custom library folder found.");
                Trace.logDebug("Folder exists check - " + Files.exists(c_dcl, new LinkOption[0]));
                Trace.logDebug("Is Directory check - " + Files.isDirectory(c_dcl, new LinkOption[0]));
            }
            Path p_dcl = dcl.resolve("product");
            if (p_dcl == null || !Files.exists(p_dcl, new LinkOption[0])) {
                p_dcl = dcl.resolve("Product");
            }
            if (p_dcl != null && Files.exists(p_dcl, new LinkOption[0]) && Files.isDirectory(p_dcl, new LinkOption[0])) {
                LabVantageClassLoader.grabJars(p_dcl, jars, excludedJars);
                Path classes = p_dcl.resolve("classes");
                if (Files.exists(classes, new LinkOption[0]) && Files.isDirectory(classes, new LinkOption[0])) {
                    try {
                        jars.add(classes.toUri().toURL());
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                loaded = true;
                Trace.logDebug("Loaded product library: " + p_dcl.toString());
            } else {
                Trace.logDebug("No product library folder found.");
                Trace.logDebug("Folder exists check - " + Files.exists(p_dcl, new LinkOption[0]));
                Trace.logDebug("Is Directory check - " + Files.isDirectory(p_dcl, new LinkOption[0]));
            }
        } else {
            Trace.logDebug("No dynamic library folder found.");
            Trace.logDebug("Folder exists check - " + Files.exists(dcl, new LinkOption[0]));
            Trace.logDebug("Is Directory check - " + Files.isDirectory(dcl, new LinkOption[0]));
        }
        return loaded;
    }

    private static LabVantageClassLoader getAppResourceClassLoader(String appresourceid, String[] excludedJars, ConnectionInfo connectionInfo) throws SapphireException {
        String classloaderid = ClassLoaderType.APPRESOURCE.getTypeName() + "_" + appresourceid;
        Object o = CacheUtil.get(connectionInfo.getDatabaseId(), "ClassLoaders", classloaderid);
        if (o == null || !(o instanceof LabVantageClassLoader)) {
            ArrayList<URL> jars = new ArrayList<URL>();
            LabVantageClassLoader l = null;
            boolean loadedDynamicPath = false;
            String dynamicclasslibary = LabVantageClassLoader.getDynamicClassPath(connectionInfo);
            if (appresourceid != null && appresourceid.length() > 0) {
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("LV_AppResource");
                sdiRequest.setRequestItem("primary");
                sdiRequest.setRequestItem("attachment");
                sdiRequest.setKeyid1List(appresourceid);
                sdiRequest.setRetainRsetid(false);
                SDIData appRes = new SDIProcessor(connectionInfo.getConnectionId()).getSDIData(sdiRequest);
                if (appRes != null && appRes.getDataset("primary") != null && appRes.getDataset("primary").getRowCount() > 0) {
                    String appResourceLibPath;
                    if (dynamicclasslibary.length() > 0 && (appResourceLibPath = appRes.getDataset("primary").getValue(0, "librarydir", "")).length() > 0) {
                        Path dcl = dynamicclasslibary.endsWith(File.separator) ? Paths.get(dynamicclasslibary + appResourceLibPath, new String[0]) : Paths.get(dynamicclasslibary + File.separator + appResourceLibPath, new String[0]);
                        loadedDynamicPath = LabVantageClassLoader.loadDynamicPath(dcl, jars, excludedJars);
                    }
                    if (appRes.getDataset("attachment") != null && appRes.getDataset("attachment").getRowCount() > 0) {
                        try {
                            LabVantageClassLoader.loadAttachments(appRes.getDataset("attachment"), null, new AttachmentProcessor(connectionInfo.getConnectionId()), jars, excludedJars);
                        }
                        catch (Exception e) {
                            Trace.logWarn(e.getMessage(), e);
                        }
                    }
                }
            }
            if (!loadedDynamicPath && dynamicclasslibary.length() > 0) {
                LabVantageClassLoader.loadDynamicPath(Paths.get(dynamicclasslibary, new String[0]), jars, excludedJars);
            }
            if (jars != null && jars.size() > 0) {
                try {
                    if (jars.size() == 1) {
                        l = new LabVantageClassLoader(new URL[]{(URL)jars.get(0)}, Thread.currentThread().getContextClassLoader());
                    }
                    URL[] urls = jars.toArray(new URL[0]);
                    l = new LabVantageClassLoader(urls, Thread.currentThread().getContextClassLoader());
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to load library.", e);
                }
            } else {
                l = new LabVantageClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());
            }
            if (l != null) {
                l.classLoaderId = classloaderid;
                l.setType(ClassLoaderType.APPRESOURCE);
                l.addToCache(classloaderid, connectionInfo.getDatabaseId());
            }
            return l;
        }
        return (LabVantageClassLoader)o;
    }

    public static LabVantageClassLoader getClassLoader(ClassLoaderType classloaderType, String classloaderid, String appresourceid, String[] excludedJars, ConnectionInfo connectionInfo) throws SapphireException {
        return LabVantageClassLoader.getClassLoader(classloaderType, classloaderid, appresourceid, null, null, excludedJars, connectionInfo);
    }

    public static LabVantageClassLoader getClassLoader(ClassLoaderType classloaderType, String classloaderid, String appresourceid, DataSet sdiattachment, String attachmentclass, String[] excludedJars, ConnectionInfo connectionInfo) throws SapphireException {
        Object o = CacheUtil.get(connectionInfo.getDatabaseId(), "ClassLoaders", classloaderType.getTypeName() + "_" + classloaderid);
        if (o == null || !(o instanceof LabVantageClassLoader)) {
            Trace.logDebug("getClassLoader Loading (1)...");
            LabVantageClassLoader l = null;
            ArrayList<URL> jars = new ArrayList<URL>();
            AttachmentProcessor attachmentProcessor = new AttachmentProcessor(connectionInfo.getConnectionId());
            if (attachmentProcessor != null) {
                if (sdiattachment != null && sdiattachment.getRowCount() > 0) {
                    try {
                        LabVantageClassLoader.loadAttachments(sdiattachment, attachmentclass, attachmentProcessor, jars, excludedJars);
                    }
                    catch (Exception e) {
                        Trace.logWarn(e.getMessage(), e);
                    }
                }
                if (jars != null && jars.size() > 0) {
                    try {
                        if (jars.size() == 1) {
                            l = new LabVantageClassLoader(new URL[]{(URL)jars.get(0)}, (ClassLoader)LabVantageClassLoader.getAppResourceClassLoader(appresourceid, excludedJars, connectionInfo));
                        }
                        URL[] urls = jars.toArray(new URL[0]);
                        l = new LabVantageClassLoader(urls, (ClassLoader)LabVantageClassLoader.getAppResourceClassLoader(appresourceid, excludedJars, connectionInfo));
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to load library.", e);
                    }
                } else {
                    l = new LabVantageClassLoader(new URL[0], (ClassLoader)LabVantageClassLoader.getAppResourceClassLoader(appresourceid, excludedJars, connectionInfo));
                }
            } else {
                l = new LabVantageClassLoader(new URL[0], (ClassLoader)LabVantageClassLoader.getAppResourceClassLoader(appresourceid, excludedJars, connectionInfo));
            }
            if (l != null) {
                ArrayList classloadersList;
                l.classLoaderId = classloaderid;
                l.setType(classloaderType);
                Object ol = CacheUtil.get(connectionInfo.getDatabaseId(), "ClassLoaders", "__classloaderslist");
                ArrayList arrayList = classloadersList = ol != null ? (ArrayList)ol : new ArrayList();
                if (!classloadersList.contains(classloaderType.getTypeName() + "_" + classloaderid)) {
                    classloadersList.add(classloaderType.getTypeName() + "_" + classloaderid);
                    CacheUtil.put(connectionInfo.getDatabaseId(), "ClassLoaders", "__classloaderslist", classloadersList);
                }
                l.addToCache(classloaderType.getTypeName() + "_" + classloaderid, connectionInfo.getDatabaseId());
            }
            return l;
        }
        Trace.logDebug("getClassLoader Loaded from cache.(1)");
        LabVantageClassLoader l = (LabVantageClassLoader)o;
        return l;
    }

    public void addAttachments(DataSet sdiattachment, String attachmentclass, String[] excludedJars, AttachmentProcessor attachmentProcessor) throws SapphireException {
        if (sdiattachment != null && sdiattachment.getRowCount() > 0) {
            for (int a = 0; a < sdiattachment.getRowCount(); ++a) {
                if (attachmentclass != null && attachmentclass.length() != 0 && !sdiattachment.getValue(a, "attachmentclass", "").equalsIgnoreCase(attachmentclass)) continue;
                try {
                    String sdcid = sdiattachment.getValue(a, "sdcid", "");
                    String keyid1 = sdiattachment.getValue(a, "keyid1", "(null)");
                    String keyid2 = sdiattachment.getValue(a, "keyid2", "(null)");
                    String keyid3 = sdiattachment.getValue(a, "keyid3", "(null)");
                    sapphire.attachment.Attachment jar = sapphire.attachment.Attachment.getAttachment(sdcid, keyid1, keyid2, keyid3, Integer.parseInt(sdiattachment.getValue(a, "attachmentnum", "")));
                    Path p = attachmentProcessor.getSDIAttachmentLocalFile((Attachment)jar, true);
                    if (p != null && Files.exists(p, new LinkOption[0])) {
                        if (Files.isDirectory(p, new LinkOption[0])) {
                            try {
                                ArrayList<URL> jars = new ArrayList<URL>();
                                LabVantageClassLoader.grabJars(p, jars, excludedJars);
                                for (URL url : jars) {
                                    String u = url.toString();
                                    if (!this.classPath.contains(u)) {
                                        this.classPath.add(u);
                                    }
                                    this.addURL(url);
                                }
                                continue;
                            }
                            catch (Exception e) {
                                throw new SapphireException("Failed to use library path. ", e);
                            }
                        }
                        String u = p.toString();
                        if (!this.classPath.contains(u)) {
                            this.classPath.add(u);
                        }
                        this.addURL(p.toUri().toURL());
                        continue;
                    }
                    throw new SapphireException("Failed to load library path");
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void addAttachment(sapphire.attachment.Attachment jar, String[] excludedJars, AttachmentProcessor attachmentProcessor) throws SapphireException {
        Path p = attachmentProcessor.getSDIAttachmentLocalFile((Attachment)jar, true);
        if (p == null || !Files.exists(p, new LinkOption[0])) throw new SapphireException("Failed to find library path. ");
        if (Files.isDirectory(p, new LinkOption[0])) {
            try {
                List subjars = Files.walk(p, new FileVisitOption[0]).filter(superPath -> FileManager.getExtension(superPath.toString()).equalsIgnoreCase("jar")).sorted().collect(Collectors.toList());
                if (subjars == null || subjars.size() <= 0) return;
                for (Path sp : subjars) {
                    String f = FileManager.getFileName(sp.toString(), false);
                    boolean found = false;
                    if (excludedJars != null) {
                        for (String f1 : excludedJars) {
                            String f2 = FileManager.getFileName(f1, false);
                            if (!f2.equalsIgnoreCase(f)) continue;
                            found = true;
                            break;
                        }
                    }
                    if (found) continue;
                    String u = sp.toString();
                    if (!this.classPath.contains(u)) {
                        this.classPath.add(u);
                    }
                    this.addURL(sp.toUri().toURL());
                }
                return;
            }
            catch (Exception e) {
                throw new SapphireException("Failed to use library path. ", e);
            }
        }
        try {
            String u = p.toString();
            if (!this.classPath.contains(u)) {
                this.classPath.add(u);
            }
            this.addURL(p.toUri().toURL());
            return;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to use library path. ", e);
        }
    }

    public void addToCache(String classloaderid, String databaseid) {
        CacheUtil.put(databaseid, "ClassLoaders", classloaderid, this);
    }

    public String getClassPath() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : this.classPath) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(";");
            }
            stringBuilder.append(s);
        }
        return stringBuilder.toString();
    }

    public List<String> getClassPathArray() {
        return this.classPath;
    }

    public LabVantageClassLoader(URL[] classpath, ClassLoader parent) {
        super(classpath, parent);
        for (URL url : classpath) {
            String s = url.getFile();
            if (s.length() > 0 && s.startsWith("/")) {
                s = s.substring(1);
            }
            this.classPath.add(s);
        }
        this.system = LabVantageClassLoader.getSystemClassLoader();
    }

    private void securityCheck(String name) throws SecurityException {
        JSONArray whiteList;
        JSONArray blackList = LabVantageSecurityManager.getClassesBlackList();
        boolean blocked = false;
        if (blackList != null && blackList.length() > 0) {
            for (int i = 0; i < blackList.length(); ++i) {
                try {
                    if (!FileUtil.wildcardMatch(name, blackList.getString(i), false)) continue;
                    blocked = true;
                    break;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        if (blocked && (whiteList = LabVantageSecurityManager.getClassesWhiteList()) != null && whiteList.length() > 0) {
            for (int i = 0; i < whiteList.length(); ++i) {
                try {
                    if (!FileUtil.wildcardMatch(name, whiteList.getString(i), false)) continue;
                    blocked = false;
                    break;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        if (blocked) {
            if (!this.failed.contains(name)) {
                this.failed.add(name);
            }
            throw new SecurityException("Class '" + name + "' not accessible from external sources.");
        }
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c;
        block15: {
            this.securityCheck(name);
            c = this.findLoadedClass(name);
            if (c == null) {
                if (name.startsWith("java.") || name.startsWith("system.") || name.startsWith("sun.")) {
                    try {
                        c = super.loadClass(name, resolve);
                    }
                    catch (ClassNotFoundException e2) {
                        if (this.system != null) {
                            c = this.system.loadClass(name);
                        }
                        break block15;
                    }
                }
                if (name.startsWith("sapphire.") && !name.startsWith("sapphire.custom.") && !name.startsWith("sapphire.plugin.") || name.startsWith("com.labvantage.") && !name.startsWith("com.labvantage.plugin.")) {
                    try {
                        c = super.loadClass(name, resolve);
                    }
                    catch (ClassNotFoundException e2) {
                        if (this.system != null) {
                            c = this.system.loadClass(name);
                        }
                        break block15;
                    }
                }
                try {
                    c = this.findClass(name);
                    if (!this.loadedClasses.contains(c)) {
                        this.loadedClasses.add(name);
                    }
                }
                catch (ClassNotFoundException e) {
                    try {
                        c = super.loadClass(name, resolve);
                    }
                    catch (ClassNotFoundException e2) {
                        if (this.system == null) break block15;
                        c = this.system.loadClass(name);
                    }
                }
            }
        }
        if (resolve) {
            this.resolveClass(c);
        }
        return c;
    }

    @Override
    public URL getResource(String name) {
        URL url = this.findResource(name);
        if (url == null) {
            url = super.getResource(name);
        }
        if (url == null && this.system != null) {
            url = this.system.getResource(name);
        }
        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> systemUrls = null;
        if (this.system != null) {
            systemUrls = this.system.getResources(name);
        }
        Enumeration<URL> localUrls = this.findResources(name);
        Enumeration<URL> parentUrls = null;
        if (this.getParent() != null) {
            parentUrls = this.getParent().getResources(name);
        }
        final ArrayList<URL> urls = new ArrayList<URL>();
        if (localUrls != null) {
            while (localUrls.hasMoreElements()) {
                URL local = localUrls.nextElement();
                urls.add(local);
            }
        }
        if (systemUrls != null) {
            while (systemUrls.hasMoreElements()) {
                urls.add(systemUrls.nextElement());
            }
        }
        if (parentUrls != null) {
            while (parentUrls.hasMoreElements()) {
                urls.add(parentUrls.nextElement());
            }
        }
        return new Enumeration<URL>(){
            Iterator<URL> iter;
            {
                this.iter = urls.iterator();
            }

            @Override
            public boolean hasMoreElements() {
                return this.iter.hasNext();
            }

            @Override
            public URL nextElement() {
                return this.iter.next();
            }
        };
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        URL url = this.getResource(name);
        try {
            return url != null ? url.openStream() : null;
        }
        catch (IOException iOException) {
            return null;
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> clazz;
        block7: {
            try {
                clazz = super.loadClass(name);
                if (this.loadedClasses.contains(name)) break block7;
                this.loadedClasses.add(name);
            }
            catch (Throwable throwable) {
                try {
                    if (!this.loadedClasses.contains(name)) {
                        this.loadedClasses.add(name);
                    }
                    throw throwable;
                }
                catch (ClassNotFoundException e) {
                    if (!this.failed.contains(name)) {
                        this.failed.add(name);
                    }
                    throw e;
                }
            }
        }
        return clazz;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    public String getId() {
        return this.classLoaderId;
    }

    public List<String> getFailedClasses() {
        return this.failed;
    }

    public List<String> getLoadedClasses() {
        return this.loadedClasses;
    }

    public boolean hasFailed() {
        return this.failed.size() > 0;
    }

    public static enum ClassLoaderType {
        ATTACHMENTHANDLER("LV_AttachmentHandler", "attachmenthandlers"),
        ATTACHMENTREPOSITORY("AttachmentRepository", "attachmentrepositories"),
        REPORT("SapphireJavaTalendReport", "reports"),
        ACTION("ActionService", "actions"),
        APPRESOURCE("AppResource", "");

        String typeName;
        String area;

        private ClassLoaderType(String name, String area) {
            this.typeName = name;
            this.area = area;
        }

        public String getTypeName() {
            return this.typeName;
        }

        public String getArea() {
            return this.area;
        }
    }
}

