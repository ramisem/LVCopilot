/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.logfileviewer;

import com.labvantage.sapphire.admin.logfileviewer.LogViewerParser;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogViewerParser_Default
implements LogViewerParser {
    private static int PARSE_DATE_FROM = 0;
    private static int PARSE_DATE_TO = 23;
    private static int PARSE_LEVEL_FROM = 24;
    private static int PARSE_LEVEL_TO = 29;
    private static int PARSE_CONNECTION_FROM = 30;
    private static int PARSE_CONNECTION_TO = 57;
    private static int THREAD_STARTS_BETWEEN_FROM = 20;
    private static int THREAD_STARTS_BETWEEN_TO = 40;
    private static int CONTENT_SEPARATOR_STARTS_AFTER = 60;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

    @Override
    public boolean isContentLine(String line) {
        int pos = line.indexOf("[#");
        return pos >= THREAD_STARTS_BETWEEN_FROM && pos < THREAD_STARTS_BETWEEN_TO;
    }

    @Override
    public String getLineContent(String line) {
        int pos = line.indexOf(":", CONTENT_SEPARATOR_STARTS_AFTER);
        String s = "";
        try {
            s = pos > CONTENT_SEPARATOR_STARTS_AFTER + 2 ? line.substring(pos + 2) : "";
        }
        catch (Exception exception) {
            // empty catch block
        }
        return s;
    }

    @Override
    public int getLineContentPos(String line) {
        int pos = line.indexOf(":", CONTENT_SEPARATOR_STARTS_AFTER);
        return pos > CONTENT_SEPARATOR_STARTS_AFTER ? pos + 2 : -1;
    }

    @Override
    public String getLineLevel(String line) {
        return line.substring(PARSE_LEVEL_FROM, PARSE_LEVEL_TO);
    }

    @Override
    public String getLineContext(String line) {
        String context = "";
        int pos = line.indexOf("[#");
        if (pos > 0) {
            int pos1 = line.indexOf(" ", pos + 1);
            int pos2 = line.indexOf(":", CONTENT_SEPARATOR_STARTS_AFTER);
            if (pos1 > 0 && pos2 > pos1) {
                context = line.substring(pos1, pos2).trim();
            }
        }
        return context;
    }

    @Override
    public String getLineDateString(String line) {
        return line.substring(PARSE_DATE_FROM, PARSE_DATE_TO);
    }

    @Override
    public Date getLineDate(String line) {
        try {
            return this.sdf.parse(line.substring(PARSE_DATE_FROM, PARSE_DATE_TO));
        }
        catch (ParseException e) {
            return null;
        }
    }

    @Override
    public String getLineThread(String line) {
        int pos2;
        String thread = "";
        int pos = line.indexOf("[#");
        if (pos >= THREAD_STARTS_BETWEEN_FROM && pos < THREAD_STARTS_BETWEEN_TO && (pos2 = line.indexOf("]", pos)) > 0) {
            thread = line.substring(pos + 2, pos2);
        }
        return thread;
    }

    @Override
    public boolean isContentRequestStart(String content) {
        return content.startsWith("++++++++++ Received request");
    }

    @Override
    public boolean isContentRequestEnd(String content) {
        return content.startsWith("++++++++++ Completed request");
    }

    @Override
    public boolean isContentActionStart(String content) {
        return content.startsWith("++++++++++ Processing Action ");
    }

    @Override
    public boolean isContentActionEnd(String content) {
        return content.startsWith("++++++++++ Completed Action ");
    }

    @Override
    public boolean isContentStartupStart(String content) {
        return content.startsWith("** Starting LabVantage");
    }

    @Override
    public boolean isContentStartupEnd(String content) {
        return content.startsWith("** Automation setup complete");
    }
}

