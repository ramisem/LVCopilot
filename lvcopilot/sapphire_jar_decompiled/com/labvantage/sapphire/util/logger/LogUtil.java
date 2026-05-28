/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.logger;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.util.logger.LogConfig;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.util.DataSet;

public class LogUtil {
    public static void setDebugEnabled(String applicationid, File configFile) throws SapphireException {
        LogConfig logConfig = new LogConfig(applicationid, configFile);
        if ("DEBUG".equalsIgnoreCase(logConfig.getRootLoggerLevel())) {
            Trace.debugEnabled = true;
        }
    }

    public static void archiveStartupLog(String applicationid, String applicationHome, File configFile) throws SapphireException {
        int backups;
        try {
            LogConfig logConfig = new LogConfig(applicationid, configFile);
            backups = Integer.parseInt(logConfig.getLogProperty("sapphire.logging.startup.maxbackupindex", "0"));
        }
        catch (NumberFormatException e) {
            backups = 0;
        }
        if (backups > 0) {
            try {
                int i;
                Calendar cal = DateTimeUtil.getNowCalendar();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                String prefix = "labvantage_startup";
                File startuplog = new File(applicationHome + File.separator + "logs" + File.separator + "labvantage_startup.log");
                if (!startuplog.exists()) {
                    startuplog = new File(applicationHome + File.separator + "logs" + File.separator + "sapphire_startup.log");
                    prefix = "sapphire_startup";
                }
                FileUtil.copyFile(startuplog, new File(applicationHome + File.separator + "logs" + File.separator + prefix + "_" + sdf.format(cal.getTime()) + ".log"));
                File logDir = new File(applicationHome + File.separator + "logs");
                File[] files = logDir.listFiles();
                DataSet ds = new DataSet();
                for (i = 0; i < files.length; ++i) {
                    if (!files[i].getName().startsWith(prefix + "_")) continue;
                    ds.setString(ds.addRow(), "filename", files[i].getName());
                }
                ds.sort("filename d");
                for (i = backups; i < ds.size(); ++i) {
                    File file = new File(applicationHome + File.separator + "logs" + File.separator + ds.getValue(i, "filename"));
                    file.delete();
                }
            }
            catch (Exception e) {
                throw new SapphireException("Failed to archive startup logs. Reason: " + e.getMessage(), e);
            }
        }
    }

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    public static String getStackTraceMessages(Throwable t, String separator, boolean removeDups, boolean removeExceptions) {
        return LogUtil.getStackTraceMessages(t, separator, removeDups, removeExceptions, false);
    }

    public static String getStackTraceMessages(Throwable t, String separator, boolean removeDups, boolean removeExceptions, boolean escapeHTML) {
        HashSet messages = new HashSet();
        return LogUtil.getStackTraceMessages(t, separator, removeDups, removeExceptions, escapeHTML, messages);
    }

    private static String getStackTraceMessages(Throwable t, String separator, boolean removeDups, boolean removeExceptions, boolean escapeHTML, HashSet messages) {
        StringBuffer out = new StringBuffer();
        if (t != null) {
            Throwable tc;
            String message = t.getMessage();
            if (message != null) {
                int pos;
                if (escapeHTML) {
                    message.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("&", "&amp;").replaceAll("\"", "&quot;");
                }
                if (removeExceptions && (pos = message.lastIndexOf("Exception:")) > -1) {
                    message = message.substring(pos + 10).trim();
                }
                if (removeDups) {
                    if (!messages.contains(message)) {
                        messages.add(message);
                        out.append(message).append(separator);
                    }
                } else {
                    out.append(message).append(separator);
                }
            }
            if ((tc = t.getCause()) != null) {
                out.append(LogUtil.getStackTraceMessages(tc, separator, removeDups, removeExceptions, escapeHTML, messages));
            }
        } else {
            out.append("Stack trace cannot be generated as it is null");
        }
        return out.toString();
    }
}

