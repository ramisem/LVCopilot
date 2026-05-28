/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.instrument.csv;

public interface CSVConstants {
    public static final char DEFAULT_SEPARATOR = ',';
    public static final char DEFAULT_QUOTE = '\"';
    public static final char DEFAULT_ESCAPE = '\\';
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final char NULL_CHARACTER = '\u0000';
    public static final char NO_QUOTE_CHARACTER = '\u0000';
    public static final String DEFAULT_LINE_END = "\n";
    public static final char NO_ESCAPE_CHARACTER = '\u0000';

    public void setCharset(String var1);
}

