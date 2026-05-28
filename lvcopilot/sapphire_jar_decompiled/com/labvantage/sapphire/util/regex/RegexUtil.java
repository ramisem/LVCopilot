/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.regex;

import java.util.regex.Pattern;

public class RegexUtil {
    public static boolean wildcardMatch(String input, String wildcard) {
        return Pattern.matches(RegexUtil.wildcardToRegex(wildcard), input);
    }

    public static String wildcardToRegex(String wildcard) {
        StringBuffer s = new StringBuffer(wildcard.length());
        s.append('^');
        int is = wildcard.length();
        block5: for (int i = 0; i < is; ++i) {
            char c = wildcard.charAt(i);
            switch (c) {
                case '*': {
                    s.append(".*");
                    continue block5;
                }
                case '?': {
                    s.append(".");
                    continue block5;
                }
                case '$': 
                case '(': 
                case ')': 
                case '.': 
                case '[': 
                case '\\': 
                case ']': 
                case '^': 
                case '{': 
                case '|': 
                case '}': {
                    s.append("\\");
                    s.append(c);
                    continue block5;
                }
                default: {
                    s.append(c);
                }
            }
        }
        s.append('$');
        return s.toString();
    }
}

