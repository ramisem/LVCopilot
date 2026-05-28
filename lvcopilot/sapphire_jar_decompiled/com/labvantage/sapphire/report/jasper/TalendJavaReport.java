/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.report.jasper;

import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileTransfer;
import com.labvantage.sapphire.util.file.FileTransferOptions;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import sapphire.SapphireException;
import sapphire.report.BaseJavaReport;
import sapphire.util.ConnectionInfo;
import sapphire.util.Logger;
import sapphire.util.StringUtil;

public class TalendJavaReport
extends BaseJavaReport {
    private String objectName = "";
    private String fileName = "";
    private Path path = null;

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void init(String reportId, String reportVerId, HashMap paramsMap, ConnectionInfo connectionInfo) throws SapphireException {
        String connectionId = connectionInfo.getConnectionId();
        if (this.objectName == null) throw new SapphireException("No Talend class provided");
        Object talendJob = null;
        Method talendJob_getContext = null;
        try {
            Class<?> classToLoad = Class.forName(this.objectName, true, this.getClassLoader());
            talendJob_getContext = classToLoad.getDeclaredMethod("getContext", new Class[0]);
            talendJob = classToLoad.newInstance();
        }
        catch (Exception e) {
            throw new SapphireException("Unable to load Talend class.");
        }
        if (talendJob == null) throw new SapphireException("Could not load Talend class provided");
        String[][] output = null;
        PrintStream oldOut = System.out;
        try {
            final StringBuilder logbuffer = new StringBuilder();
            PrintStream printStream = new PrintStream(new OutputStream(){

                @Override
                public void write(int b) throws IOException {
                    logbuffer.append(String.valueOf((char)b));
                }
            });
            System.setOut(printStream);
            ArrayList<String> args = new ArrayList<String>();
            args.add("--context_param connectionid=" + connectionId);
            args.add("--context_param reportid=" + reportId);
            args.add("--context_param reportversionid=" + reportVerId);
            if (paramsMap != null && paramsMap.size() > 0) {
                for (Object k : paramsMap.keySet()) {
                    Object v = paramsMap.get(k);
                    if (!(k instanceof String) || !(v instanceof String) || ((String)v).length() <= 0) continue;
                    if (((String)k).toLowerCase().startsWith("property_")) {
                        args.add("--context_param " + k.toString() + "=" + v.toString());
                        continue;
                    }
                    args.add("--context_param property_" + k.toString() + "=" + v.toString());
                }
            }
            try {
                Class[] cArg = new Class[]{String[].class};
                Method runJobMethod = talendJob.getClass().getMethod("runJob", cArg);
                Object outputOb = runJobMethod.invoke(talendJob, new Object[]{args.toArray(new String[0])});
                if (outputOb instanceof String[][]) {
                    output = (String[][])outputOb;
                }
                this.logger.debug("TalendJavaReport - Processed Job.");
            }
            catch (Exception e) {
                this.logger.error(e.getMessage());
                throw new SapphireException("Failed to run job. Please check all supporting JAR files are included.", e);
            }
            if (logbuffer != null && logbuffer.length() > 0) {
                this.logger.debug("TalendJavaReport - Log obtained from talend:\n-----------------START Talend Log------------------\n" + logbuffer.toString() + "\n-----------------END Talend Log------------------\n");
            } else {
                this.logger.debug("TalendJavaReport - No log obtained from Talend.");
            }
        }
        finally {
            System.setOut(oldOut);
        }
        if (output == null || !output[0][0].equals("0")) {
            throw new SapphireException("Failed to RunJob in Talend");
        }
        Logger.logDebug("Talend RunJob succeeded");
        Properties contextProperties = null;
        try {
            Object result = talendJob_getContext != null ? talendJob_getContext.invoke(talendJob, new Object[0]) : null;
            contextProperties = result != null ? (Properties)result : null;
        }
        catch (Exception e3) {
            throw new SapphireException("Unable to get context properties");
        }
        String contextFiles = "LabVantageFileOutput_EXEC";
        if (contextProperties == null) return;
        if (!contextProperties.containsKey(contextFiles)) return;
        String[] exec = StringUtil.split(contextProperties.getProperty(contextFiles), ";");
        for (int i = 0; i < exec.length; ++i) {
            String file = exec[i];
            String filepathcontext = StringUtil.replaceAll(contextFiles, "_EXEC", "_PATH_") + file;
            if (!contextProperties.containsKey(filepathcontext)) continue;
            String filepath = contextProperties.getProperty(filepathcontext);
            String filename = "";
            String filenamecontext = StringUtil.replaceAll(contextFiles, "_EXEC", "_NAME_") + file;
            if (contextProperties.containsKey(filenamecontext)) {
                filename = contextProperties.getProperty(filenamecontext);
            }
            if (filename.length() == 0) {
                filename = FileManager.getFileName(filepath, true);
            }
            this.fileName = filename;
            this.path = Paths.get(filepath, new String[0]);
            return;
        }
    }

    @Override
    public String getLogicalFileName(String defaultFileName) {
        return defaultFileName.length() > 0 ? defaultFileName : this.fileName;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void runReport(OutputStream outputStream) throws SapphireException {
        if (this.path == null) throw new SapphireException("Talend report returned no file.");
        if (!Files.exists(this.path, new LinkOption[0])) throw new SapphireException("Failed to find file output by Talend report.");
        FileTransferOptions fto = new FileTransferOptions();
        fto.setCloseOutputStream(false);
        fto.setCloseInputStream(true);
        try {
            FileTransfer.safeDataTransfer(Files.newInputStream(this.path, new OpenOption[0]), outputStream, fto);
            return;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to load final output file from Talend.");
        }
    }
}

