/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import sapphire.util.StringUtil;

public class BaseCrypt {
    private static final String _realchars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz!\u00a3$%^&*()_-+=[]:;@#|\\/<>,.?'~\"";
    private static final String _convchars = "09PLKAMB1N28RGJ6C5ZOYD3S4WEUHIQFVT7Xkwictabpqgzoedvsmfluryhnjx&$!)+@>:([=#/<\\.],|?~\"'\u00a3%^*_;-";

    private static char realToConv(char in) {
        char out = ' ';
        int i = _realchars.indexOf(in);
        out = i >= 0 ? (char)_convchars.charAt(i) : (char)in;
        return out;
    }

    private static char convToReal(char in) {
        char out = ' ';
        int i = _convchars.indexOf(in);
        out = i >= 0 ? (char)_realchars.charAt(i) : (char)in;
        return out;
    }

    public static String encrypt(String input) {
        int chars;
        StringBuffer endResult = new StringBuffer("");
        int n = chars = input == null ? 0 : input.length();
        if (chars > 0) {
            int i;
            int poscount = 0;
            for (int i2 = 0; i2 < chars; ++i2) {
                poscount += _realchars.indexOf(input.charAt(i2)) + 1;
            }
            int t = poscount % 62 - 1;
            if (t < 0) {
                t = 0;
            }
            input = input + _realchars.charAt(t);
            ++chars;
            poscount = 0;
            for (i = 0; i < chars; ++i) {
                poscount += _realchars.indexOf(input.charAt(i)) + 1;
            }
            t = poscount % 62 - 1;
            if (t < 0) {
                t = 0;
            }
            input = _realchars.charAt(t) + input;
            ++chars;
            for (i = 0; i < chars; ++i) {
                endResult.append(BaseCrypt.realToConv(input.charAt(i)));
            }
            StringBuffer temp = endResult;
            endResult = new StringBuffer("");
            int hyphens = 0;
            for (int i3 = 0; i3 < chars + hyphens; ++i3) {
                if ((i3 + 1) % 5 == 0) {
                    endResult.append("-");
                    ++hyphens;
                    continue;
                }
                endResult.append(temp.charAt(i3 - hyphens));
            }
        }
        return StringUtil.replaceAll(endResult.toString(), ";", "\u00ac");
    }

    public static String decrypt(String input) {
        int chars;
        String output = "";
        input = input == null ? input : StringUtil.replaceAll(input, "\u00ac", ";");
        int n = chars = input == null ? 0 : input.length();
        if (chars > 0) {
            StringBuffer temp = new StringBuffer("");
            for (int i = 0; i < chars; ++i) {
                if (input.charAt(i) == '-') continue;
                temp.append(input.charAt(i));
            }
            StringBuffer temp2 = new StringBuffer("");
            char initialcheck = ' ';
            for (int i = 0; i < temp.length(); ++i) {
                if (i == 0) {
                    initialcheck = BaseCrypt.convToReal(temp.charAt(i));
                    continue;
                }
                temp2.append(BaseCrypt.convToReal(temp.charAt(i)));
            }
            int poscount = 0;
            for (int i = 0; i < temp2.length(); ++i) {
                poscount += _realchars.indexOf(temp2.charAt(i)) + 1;
            }
            int t = poscount % 62 - 1;
            if (t < 0) {
                t = 0;
            }
            if (initialcheck == _realchars.charAt(t)) {
                char finalcheck = temp2.charAt(temp2.length() - 1);
                temp2.setLength(temp2.length() - 1);
                poscount = 0;
                for (int i = 0; i < temp2.length(); ++i) {
                    poscount += _realchars.indexOf(temp2.charAt(i)) + 1;
                }
                t = poscount % 62 - 1;
                if (t < 0) {
                    t = 0;
                }
                if (finalcheck == _realchars.charAt(t)) {
                    output = temp2.toString();
                }
            }
        }
        return output;
    }
}

