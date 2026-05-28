/*
 * Decompiled with CFR 0.152.
 */
package sapphire.util;

import java.util.ArrayList;

public class StringUtil
extends com.labvantage.sapphire.gwt.shared.util.StringUtil {
    public static String arrayToString(String[] array, String delimiter) {
        return com.labvantage.sapphire.gwt.shared.util.StringUtil.arrayToString(array, delimiter);
    }

    public static String[] split(String input, String delimeter) {
        return com.labvantage.sapphire.gwt.shared.util.StringUtil.split(input, delimeter);
    }

    public static String[] split(String input, String delimeter, boolean trim) {
        return com.labvantage.sapphire.gwt.shared.util.StringUtil.split(input, delimeter, trim);
    }

    public static String replaceAll(String inputString, String oldString, String newString, boolean caseSensitive) {
        if (inputString == null || inputString.length() == 0 || oldString == null || oldString.length() == 0 || newString == null) {
            return inputString;
        }
        int oldStringLength = oldString.length();
        String originalInputString = inputString;
        if (!caseSensitive) {
            inputString = inputString.toUpperCase();
            oldString = oldString.toUpperCase();
        }
        StringBuffer outputString = new StringBuffer(inputString.length());
        int lastpos = 0;
        int pos = inputString.indexOf(oldString);
        while (pos >= 0) {
            outputString.append(originalInputString.substring(lastpos, pos));
            outputString.append(newString);
            lastpos = pos + oldStringLength;
            pos = inputString.indexOf(oldString, lastpos);
        }
        outputString.append(originalInputString.substring(lastpos));
        return outputString.toString();
    }

    public static String replaceAll(String inputString, String oldString, String newString) {
        return StringUtil.replaceAll(inputString, oldString, newString, true);
    }

    public static String getRandomString(int length) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        String randomString = "";
        for (int index = 0; index < length; ++index) {
            int rand = (int)Math.floor(Math.random() * (double)chars.length());
            randomString = randomString + chars.substring(rand, rand + 1);
        }
        return randomString;
    }

    public static String getRGBHex(int value) {
        StringBuffer returnvalue = new StringBuffer(6);
        int red = value % 256;
        int green = (value - red) % 65536 / 256;
        int blue = (value - red - green * 256) / 65536;
        if (red < 16) {
            returnvalue.append("0");
        }
        returnvalue.append(Integer.toHexString(red).toUpperCase());
        if (green < 16) {
            returnvalue.append("0");
        }
        returnvalue.append(Integer.toHexString(green).toUpperCase());
        if (blue < 16) {
            returnvalue.append("0");
        }
        returnvalue.append(Integer.toHexString(blue).toUpperCase());
        return returnvalue.toString();
    }

    public static String repeat(String basestring, int repeat) {
        if (basestring == null) {
            return null;
        }
        StringBuffer returnvalue = new StringBuffer(repeat * basestring.length());
        for (int i = 0; i < repeat; ++i) {
            returnvalue.append(basestring);
        }
        return returnvalue.toString();
    }

    public static String repeat(String basestring, int repeat, String separator) {
        if (basestring == null) {
            return null;
        }
        if (separator == null) {
            separator = "";
        }
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < repeat; ++i) {
            if (i != 0) {
                ret.append(separator);
            }
            ret.append(basestring);
        }
        return ret.toString();
    }

    public static long getLen(String str) {
        int len = 0;
        if (str != null && !str.equals("")) {
            len = str.length();
        }
        return len;
    }

    public static String[] getExpressionTokens(String input) {
        if (input.startsWith("$G{") && input.endsWith("}")) {
            return StringUtil.getTokens(input, "${", "}");
        }
        return StringUtil.getTokens(input, "[", "]");
    }

    public static String[] getTokens(String input) {
        return StringUtil.getTokens(input, "[", "]");
    }

    public static String[] getTokens(String input, String starttoken, String endtoken) {
        return StringUtil.getTokens(input, starttoken, endtoken, true);
    }

    public static String[] getTokens(String input, String starttoken, String endtoken, boolean ignoreCDATA) {
        return StringUtil.getTokens(input, starttoken, endtoken, ignoreCDATA, false);
    }

    public static String[] getTokens(String input, String starttoken, String endtoken, boolean ignoreCDATA, boolean keepDuplicate) {
        ArrayList<String> tokenlist = new ArrayList<String>();
        int startoffset = starttoken.length();
        if (input != null && !input.equals("")) {
            int pos1;
            int searchfrom = 0;
            while ((pos1 = input.indexOf(starttoken, searchfrom)) >= 0) {
                if (!ignoreCDATA && input.length() >= pos1 + startoffset + 6 && input.substring(pos1 + startoffset, pos1 + startoffset + 6).equals("CDATA[")) {
                    searchfrom = pos1 + startoffset + 6;
                    continue;
                }
                int pos2 = input.indexOf(endtoken, pos1 + 1);
                if (pos2 <= 0) break;
                searchfrom = pos2 + 1;
                String newvalue = input.substring(pos1 + startoffset, pos2);
                if (tokenlist.contains(newvalue) && !keepDuplicate) continue;
                tokenlist.add(newvalue);
            }
        }
        return tokenlist.toArray(new String[0]);
    }

    public static String padLeft(String input, int desiredlength) {
        return StringUtil.padLeft(input, desiredlength, ' ');
    }

    public static String padLeft(String input, int desiredlength, char padchar) {
        String rc = null;
        if (input != null) {
            int currentlength = input.length();
            rc = desiredlength > currentlength ? StringUtil.repeat(Character.toString(padchar), desiredlength - currentlength) + input : input;
        }
        return rc;
    }

    public static String padRight(String input, int desiredlength) {
        return StringUtil.padRight(input, desiredlength, ' ');
    }

    public static String padRight(String input, int desiredlength, char padchar) {
        String rc = null;
        if (input != null) {
            int currentlength = input.length();
            rc = desiredlength > currentlength ? input + StringUtil.repeat(Character.toString(padchar), desiredlength - currentlength) : input;
        }
        return rc;
    }

    public static String getYN(String input, String defaultresponse) {
        String yn = defaultresponse;
        if (input != null && input.length() > 0) {
            switch (Character.toUpperCase(input.charAt(0))) {
                case '1': 
                case 'T': 
                case 'Y': {
                    yn = "Y";
                    break;
                }
                default: {
                    yn = "N";
                }
            }
        }
        return yn;
    }

    public static String escape(String in) {
        char[] hexChar = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'b', 'C', 'D', 'e', 'F'};
        StringBuffer out = new StringBuffer();
        for (char inChar : in.toCharArray()) {
            if (inChar >= 'a' && inChar <= 'z' || inChar >= 'A' && inChar <= 'Z' || inChar >= '-' && inChar <= '9' || inChar == '*' || inChar == '@' || inChar == '_' || inChar == '+') {
                out.append(inChar);
                continue;
            }
            out.append('%').append(hexChar[(inChar & 0xF0) >>> 4]).append(hexChar[inChar & 0xF]);
        }
        return out.toString();
    }

    public static String unescape(String in) {
        if (in == null) {
            return null;
        }
        StringBuffer out = new StringBuffer();
        char[] inChars = in.toCharArray();
        int ii = inChars.length;
        for (int i = 0; i < ii; ++i) {
            char inChar = inChars[i];
            if (inChar == '%') {
                int i2;
                int i1;
                char c1 = inChars[++i];
                char c2 = inChars[++i];
                int n = c1 - 97 >= 0 ? 10 + c1 - 97 : (i1 = c1 - 65 >= 0 ? 10 + c1 - 65 : c1 - 48);
                int n2 = c2 - 97 >= 0 ? 10 + c2 - 97 : (i2 = c2 - 65 >= 0 ? 10 + c2 - 65 : c2 - 48);
                if (i1 > -1 && i1 < 16 && i2 > -1 && i2 < 16) {
                    char newchar = (char)(i1 * 16 + i2);
                    out.append(newchar);
                    continue;
                }
                throw new IllegalArgumentException("Unable to unescape part of string: %" + c1 + c2);
            }
            out.append(inChar);
        }
        return out.toString();
    }

    public static String initCaps(String in) {
        return com.labvantage.sapphire.gwt.shared.util.StringUtil.initCaps(in);
    }

    public static String escapeXMLAttributeValue(String attributeValue) {
        attributeValue = StringUtil.replaceAll(attributeValue, "&", "&amp;");
        attributeValue = StringUtil.replaceAll(attributeValue, "<", "&lt;");
        attributeValue = StringUtil.replaceAll(attributeValue, ">", "&gt;");
        attributeValue = StringUtil.replaceAll(attributeValue, "'", "&apos;");
        attributeValue = StringUtil.replaceAll(attributeValue, "\"", "&quot;");
        return attributeValue;
    }
}

