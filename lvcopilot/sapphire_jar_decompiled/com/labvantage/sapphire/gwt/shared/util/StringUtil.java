/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.gwt.shared.util;

import java.util.ArrayList;

public class StringUtil {
    public static String arrayToString(String[] array, String delimiter) {
        if (delimiter == null || delimiter.length() == 0) {
            delimiter = ";";
        }
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            if (i > 0) {
                out.append(delimiter);
            }
            out.append(array[i]);
        }
        return out.toString();
    }

    public static String[] split(String input, String delimeter) {
        return StringUtil.split(input, delimeter, false);
    }

    public static String[] split(String input, String delimeter, boolean trim) {
        ArrayList<String> tokenlist = new ArrayList<String>();
        if (input != null) {
            if (delimeter != null) {
                int pos = input.indexOf(delimeter);
                if (pos == -1) {
                    tokenlist.add(input);
                } else {
                    int offset = 0;
                    while (pos > -1) {
                        tokenlist.add(input.substring(offset, pos));
                        offset = pos + delimeter.length();
                        pos = input.indexOf(delimeter, offset);
                    }
                    tokenlist.add(input.substring(offset, input.length()));
                }
            } else {
                tokenlist.add(input);
            }
        }
        String[] tokens = new String[tokenlist.size()];
        for (int i = 0; i < tokenlist.size(); ++i) {
            tokens[i] = trim ? ((String)tokenlist.get(i)).trim() : (String)tokenlist.get(i);
        }
        return tokens;
    }

    public static String initCaps(String in) {
        if (in == null) {
            return null;
        }
        StringBuffer out = new StringBuffer(in);
        if (in != null && in.length() > 0) {
            if (out.charAt(0) != ' ') {
                out.setCharAt(0, Character.toUpperCase(out.charAt(0)));
            }
            for (int i = 0; i < out.length() - 1; ++i) {
                if (out.charAt(i) != ' ') continue;
                out.setCharAt(i + 1, Character.toUpperCase(out.charAt(i + 1)));
            }
        }
        return out.toString();
    }
}

