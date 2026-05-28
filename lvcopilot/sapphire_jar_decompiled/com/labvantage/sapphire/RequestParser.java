/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestParser {
    public static final String ATTRIBUTES_SEP = "{+}";
    private static final String requestItem = "[^\\[\\], ]+(\\[[^\\[\\]]*\\])*((\\s)*[,]|\\z)";
    private static final String colid = "(^[^\\(\\)\\[\\],/\\*\\+\\-.\\|\\s\\']+)";
    private static final String alias = "((\\s)+([^\\(\\)\\[\\],\\s]+)(\\z))";
    private static final Pattern aliasPattern = Pattern.compile("((\\s)+([^\\(\\)\\[\\],\\s]+)(\\z))");
    private static final Pattern selectPattern = Pattern.compile("(s|S)(e|E)(l|L)(e|E)(c|C)(t|T)");
    private static final Pattern colidWithAliasPattern = Pattern.compile("(^[^\\(\\)\\[\\],/\\*\\+\\-.\\|\\s\\']+)((\\s)+([^\\(\\)\\[\\],\\s]+)(\\z))");
    private static final Pattern sdinamePattern = Pattern.compile("(pr|wf|at|ds|di|dl|dv|dp|sp|sr|ad|cc|pl|ct|rl|wi|pli|cli|wgi|wgp)(\\d)+_");

    public static boolean isSDIName(String s) {
        Matcher matcher = sdinamePattern.matcher(s);
        return matcher.find();
    }

    public static String[] parseRequestItem(String request) {
        Pattern pattern = Pattern.compile(requestItem);
        return RequestParser.getTokenList(request, pattern).toArray(new String[0]);
    }

    public static String parseAlias(String col) {
        Matcher matcher = aliasPattern.matcher(col);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return col;
    }

    public static String parseColumn(String col) {
        Matcher matcher = aliasPattern.matcher(col);
        return matcher.replaceFirst(" ").trim();
    }

    public static boolean isSelect(String s) {
        Matcher matcher = selectPattern.matcher(s);
        return matcher.find();
    }

    public static boolean isColidWithAlias(String s) {
        Matcher matcher = colidWithAliasPattern.matcher(s);
        return matcher.find();
    }

    public static String[] parseColItem(String requestitem) {
        String colitemstring = "";
        if (requestitem.indexOf("[") > 0) {
            colitemstring = requestitem.substring(requestitem.indexOf("[") + 1, requestitem.indexOf("]"));
        }
        if (colitemstring.length() == 0) {
            return new String[0];
        }
        ArrayList<String> tokenList = new ArrayList<String>();
        int startpos = 0;
        int pos = 0;
        while ((pos = colitemstring.indexOf(44, startpos)) > 0) {
            String col = colitemstring.substring(0, pos);
            if (RequestParser.isValidColumn(col)) {
                tokenList.add(col.trim());
                colitemstring = colitemstring.substring(pos + 1);
                startpos = 0;
                continue;
            }
            startpos = pos + 1;
        }
        if (RequestParser.isValidColumn(colitemstring)) {
            tokenList.add(colitemstring.trim());
        }
        return tokenList.toArray(new String[0]);
    }

    private static boolean isValidColumn(String col) {
        return col.trim().length() > 0 && RequestParser.countChar('(', col) == RequestParser.countChar(')', col) && RequestParser.countChar('\'', col) % 2 == 0;
    }

    private static int countChar(char c, String s) {
        int count = 0;
        while (s.indexOf(c) >= 0) {
            ++count;
            s = s.substring(s.indexOf(c) + 1);
        }
        return count;
    }

    private static ArrayList getTokenList(String input, Pattern pattern) {
        ArrayList<String> tokenList = new ArrayList<String>();
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String matched = matcher.group();
            matched = RequestParser.clean(matched);
            tokenList.add(matched);
        }
        return tokenList;
    }

    private static String clean(String matched) {
        String item = matched.trim();
        if (item.indexOf(",") == 0) {
            item = item.substring(1);
        }
        if (item.lastIndexOf(",") == item.length() - 1) {
            item = item.substring(0, item.length() - 1);
        }
        return item.trim();
    }

    public static String[] parseFormAttributes(String attribute) {
        ArrayList<String> tokenList = new ArrayList<String>();
        int current = 0;
        int fromindex = 0;
        for (int count = 0; (current = attribute.indexOf(ATTRIBUTES_SEP, fromindex)) >= 0 && count < 100; ++count) {
            String token = attribute.substring(0, current);
            if (RequestParser.countChar('[', token) == RequestParser.countChar(']', token)) {
                tokenList.add(token);
                attribute = attribute.substring(current + ATTRIBUTES_SEP.length());
                fromindex = 0;
                continue;
            }
            fromindex = current + ATTRIBUTES_SEP.length();
        }
        tokenList.add(attribute);
        return tokenList.toArray(new String[0]);
    }
}

