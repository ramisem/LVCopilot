/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.groovy;

import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class PropertyUtil {
    public static final String DEFAULT_SEPARATOR = ";";

    public static String repeat(String basestring, int repeat, String separator) {
        return StringUtil.repeat(basestring, repeat, separator);
    }

    public static String repeat(String basestring, int repeat) {
        return StringUtil.repeat(basestring, repeat, DEFAULT_SEPARATOR);
    }

    public static String replaceListItems(String input, String patterns, String substitutes) throws SapphireException {
        return PropertyUtil.replaceListItems(input, patterns, substitutes, DEFAULT_SEPARATOR);
    }

    public static String replaceListItems(String input, String patterns, String substitutes, String separator) throws SapphireException {
        String[] substituteList;
        if (separator == null || separator.length() == 0) {
            throw new SapphireException("Delimiter not specified.");
        }
        String[] patternList = StringUtil.split(patterns, separator);
        if (patternList.length != (substituteList = StringUtil.split(substitutes, separator)).length) {
            throw new SapphireException("The list of patterns/substitutions do not match in length");
        }
        String[] inputList = StringUtil.split(input, separator);
        String[] outputList = new String[inputList.length];
        for (int i = 0; i < inputList.length; ++i) {
            int pos = PropertyUtil.findInputPattern(inputList[i], patternList);
            outputList[i] = pos != -1 ? substituteList[pos] : inputList[i];
        }
        String ret = "";
        for (int i = 0; i < outputList.length; ++i) {
            if (i != 0) {
                ret = ret + separator;
            }
            ret = ret + outputList[i];
        }
        return ret;
    }

    public static String addListItem(String basestring, String listitem) {
        return PropertyUtil.addListItem(basestring, listitem, DEFAULT_SEPARATOR);
    }

    public static String addListItem(String basestring, String listitem, String separator) {
        if (basestring == null || basestring.length() == 0) {
            return listitem;
        }
        StringBuffer ret = new StringBuffer();
        ret.append(basestring);
        ret.append(separator);
        ret.append(listitem);
        return ret.toString();
    }

    public static boolean isListItem(String basestring, String listitem) {
        return PropertyUtil.isListItem(basestring, listitem, DEFAULT_SEPARATOR);
    }

    public static boolean isListItem(String basestring, String listitem, String separator) {
        if (basestring == null || basestring.length() == 0) {
            if (listitem == null || listitem.length() == 0) {
                return true;
            }
        } else {
            String[] arr = StringUtil.split(basestring, separator);
            for (int i = 0; i < arr.length; ++i) {
                if (!listitem.equals(arr[i])) continue;
                return true;
            }
        }
        return false;
    }

    public static int getListItemCount(String basestring) {
        return PropertyUtil.getListItemCount(basestring, DEFAULT_SEPARATOR);
    }

    public static int getListItemCount(String basestring, String separator) {
        if (basestring == null || basestring.length() == 0) {
            return 0;
        }
        return StringUtil.split(basestring, separator).length;
    }

    public static String getListItem(String basestring, int pos) {
        return PropertyUtil.getListItem(basestring, pos, DEFAULT_SEPARATOR);
    }

    public static String getListItem(String basestring, int pos, String separator) {
        if (basestring == null || basestring.length() == 0) {
            return "";
        }
        String[] arr = StringUtil.split(basestring, separator);
        if (pos > arr.length) {
            return null;
        }
        return arr[pos];
    }

    public static String getUniqueItems(String list) {
        return PropertyUtil.getUniqueItems(list, DEFAULT_SEPARATOR);
    }

    public static String getUniqueItems(String list, String separator) {
        String[] distinctList = PropertyUtil.getUniqueItems(StringUtil.split(list, separator));
        String ret = "";
        for (int i = 0; i < distinctList.length; ++i) {
            if (i != 0) {
                ret = ret + separator;
            }
            ret = ret + distinctList[i];
        }
        return ret;
    }

    public static String[] getUniqueItems(String[] list) {
        ArrayList<String> distinctList = new ArrayList<String>();
        for (int i = 0; i < list.length; ++i) {
            if (distinctList.contains(list[i])) continue;
            distinctList.add(list[i]);
        }
        String[] ret = new String[distinctList.size()];
        return distinctList.toArray(ret);
    }

    private static int findInputPattern(String input, String[] patternList) {
        for (int i = 0; i < patternList.length; ++i) {
            if (!patternList[i].equals(input)) continue;
            return i;
        }
        return -1;
    }
}

