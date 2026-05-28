/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.Logger;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import sapphire.util.StringUtil;

public class StringLogger
implements Logger,
Serializable {
    public static final int STYLE_HTMLLIST = 0;
    public static final int STYLE_HTMLTABLE = 1;
    public static final int STYLE_LIST = 2;
    private boolean showTimeStamp = true;
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss SSS");
    private int indentCount = 0;
    StringBuffer out = new StringBuffer();

    public StringLogger() {
    }

    public StringLogger(String input) {
        this.out.append((this.showTimeStamp ? sdf.format(Calendar.getInstance().getTime()) + " - " : "") + input);
    }

    @Override
    public void log(String message) {
        this.out.append((this.showTimeStamp ? sdf.format(Calendar.getInstance().getTime()) + " - " : "") + (this.indentCount > 0 ? StringUtil.repeat("\t", this.indentCount) : "") + message).append("\n");
    }

    public String getLog() {
        return this.out.toString();
    }

    public String getFormattedLog(int style) {
        int max = 1000000;
        String output = this.out.length() > max ? this.out.substring(0, max) + " ....... (log truncated)" : this.out.toString();
        switch (style) {
            case 0: {
                output = StringUtil.replaceAll(output, "\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
                output = StringUtil.replaceAll(output, "\n", "<br>");
                break;
            }
            case 1: {
                output = StringUtil.replaceAll(output, "\t", "&nbsp;</td><td>");
                output = StringUtil.replaceAll(output, "\n", "</td></tr><tr><td>");
                output = "<table cellspacing=\"0\"><tr><td>" + output + "</td></tr></table>";
            }
        }
        return output;
    }

    public void clear() {
        this.out.setLength(0);
    }

    public void increaseIndent() {
        ++this.indentCount;
    }

    public void decreaseIndent() {
        --this.indentCount;
        if (this.indentCount < 0) {
            this.indentCount = 0;
        }
    }

    public void resetIndent() {
        this.indentCount = 0;
    }
}

