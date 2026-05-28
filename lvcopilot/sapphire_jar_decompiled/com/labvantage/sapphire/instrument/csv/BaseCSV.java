/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.instrument.csv;

import com.labvantage.sapphire.instrument.csv.CSVConstants;
import java.util.HashMap;
import sapphire.util.StringUtil;

public abstract class BaseCSV
implements CSVConstants {
    protected char separator = (char)44;
    protected char quote = (char)34;
    protected char escape = (char)92;
    protected String charset = "UTF-8";
    protected boolean headerrow = true;
    protected String nullvalue = "";
    protected String lineEnd = "\n";
    protected boolean strictQuotes = false;
    protected boolean ignoreLeadingWhiteSpace = false;
    protected HashMap<String, String> columnMapping = new HashMap();

    public void setNullValue(String nullvalue) {
        this.nullvalue = nullvalue;
    }

    public void setLineEnd(String lineEnd) {
        this.lineEnd = lineEnd;
    }

    public void setColumnMapping(String columnMapping) {
        this.columnMapping = new HashMap();
        String[] split = StringUtil.split(columnMapping, ";");
        for (int i = 0; i < split.length; ++i) {
            String[] splitSub;
            String splitItem = split[i];
            if (splitItem.length() <= 0 || (splitSub = StringUtil.split(splitItem, "=")).length <= 1) continue;
            String name = splitSub[0].trim();
            String value = splitSub[1].trim();
            if (name.length() <= 0 || value.length() <= 0) continue;
            this.columnMapping.put(name, value);
        }
    }

    public void setColumnMapping(HashMap<String, String> columnMapping) {
        this.columnMapping = columnMapping;
    }

    public void setStringQuotes(boolean strictQuotes) {
        this.strictQuotes = strictQuotes;
    }

    public void setIgnoreLeadingWhiteSpace(boolean ignoreLeadingWhiteSpace) {
        this.ignoreLeadingWhiteSpace = ignoreLeadingWhiteSpace;
    }

    @Override
    public void setCharset(String charset) {
        this.charset = charset;
    }
}

