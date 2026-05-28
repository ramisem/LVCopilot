/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  groovy.lang.Binding
 *  groovy.lang.MissingPropertyException
 */
package com.labvantage.sapphire.instrument.textparser;

import groovy.lang.Binding;
import groovy.lang.MissingPropertyException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sapphire.util.StringUtil;

public class ParsingScriptPolisher {
    private static final String line = "((line)\\s+)";
    private static final String col = "((column|col)\\s+)";
    private static final String endingmarker = "((\\s*(\\s|,|;)?\\s*)|($)|($$))";
    private static final String literalmarker = "(literal)[0-9]+";
    private static final String regexmarker = "(regex)[0-9]+";
    private static final String marker = "(((literal)[0-9]+)|((regex)[0-9]+))";
    private static final String extract = "(extract)\\s+";
    private static final String nTOn = "(([0-9]+\\s+to\\s+[0-9]+)|([0-9]+,[0-9]+))";
    private static final Pattern lineposTolineposPattern = Pattern.compile("((((line)\\s+)(([0-9]+\\s+to\\s+[0-9]+)|([0-9]+,[0-9]+)))|(((line)\\s+)[0-9]+\\s+to\\s+((line)\\s+)[0-9]+)|(((line)\\s+)[0-9]+\\s((column|col)\\s+)[0-9]+\\s+to\\s+((line)\\s+)[0-9]+\\s+((column|col)\\s+)[0-9]+))((\\s*(\\s|,|;)?\\s*)|($)|($$))", 2);
    private static final Pattern tolinecolPattern = Pattern.compile("to\\s+((line)\\s+)[0-9]+\\s+((column|col)\\s+)[0-9]+((\\s*(\\s|,|;)?\\s*)|($)|($$))", 2);
    private static final Pattern fromlinecolPattern = Pattern.compile("((line)\\s+)[0-9]+\\s+((column|col)\\s+)[0-9]+((\\s*(\\s|,|;)?\\s*)|($)|($$))", 2);
    private static final Pattern extractByRegexFirstLastPattern = Pattern.compile("(extract)\\s+((first|last)\\s+)?(regex)[0-9]+((\\s*(\\s|,|;)?\\s*)|($)|($$))", 2);
    private static final Pattern extractFirstLastNPattern = Pattern.compile("(extract)\\s+((first|last)\\s+)?[0-9]+((\\s*(\\s|,|;)?\\s*)|($)|($$))", 2);
    private static final Pattern extractFirstLastNumberPattern = Pattern.compile("(extract)\\s+((first|last)\\s+)?(number|num)((\\s*(\\s|,|;)?\\s*)|($)|($$))", 2);
    private static final Pattern extractFirstLastNumberFormatPattern = Pattern.compile("(extract)\\s+((first|last)\\s+)?(number|num)\\s+(((literal)[0-9]+)|((regex)[0-9]+))((\\s*(\\s|,|;)?\\s*)|($)|($$))", 2);
    private static final Pattern extractFirstLastDatePattern = Pattern.compile("(extract)\\s+((first|last)\\s+)?(date)((\\s*(\\s|,|;)?\\s*)|($)|($$))((\\s*(\\s|,|;)?\\s*)|($)|($$))", 2);
    private static final Pattern extractFirstLastDateFormatPattern = Pattern.compile("(extract)\\s+((first|last)\\s+)?(date)\\s+(((literal)[0-9]+)|((regex)[0-9]+))((\\s*(\\s|,|;)?\\s*)|($)|($$))((\\s*(\\s|,|;)?\\s*)|($)|($$))", 2);
    private static final Pattern tokenPattern = Pattern.compile("((token)\\s+[0-9]+)((\\s*(\\s|,|;)?\\s*)|($)|($$))", 2);
    private static final Pattern linePattern = Pattern.compile("((((line)\\s+)[0-9]+))((\\s*(\\s|,|;)?\\s*)|($)|($$))", 2);
    private static final Pattern posPattern = Pattern.compile("((pos\\s+(([0-9]+\\s+to\\s+[0-9]+)|([0-9]+,[0-9]+)))|(pos\\s+[0-9]+))((\\s*(\\s|,|;)?\\s*)|($)|($$))", 2);
    private static final Pattern colPattern = Pattern.compile("((((column|col)\\s+)(([0-9]+\\s+to\\s+[0-9]+)|([0-9]+,[0-9]+)))|(((column|col)\\s+)[0-9]+))((\\s*(\\s|,|;)?\\s*)|($)|($$))", 2);
    private static final Pattern beforeAfterPattern = Pattern.compile("(((extract)\\s+)|((extract)\\s+all\\s+))?((before)|(after))\\s+(((literal)[0-9]+)|((regex)[0-9]+))((\\s*(\\s|,|;)?\\s*)|($)|($$))", 2);
    private static final String validPatternStr = "(splitby (((literal)[0-9]+)|((regex)[0-9]+));)|((extractNumberLast\\(\\)|extractNumber\\(\\)|extractDateLast\\(\\)|extractDate\\(\\)|extract\\(\\))(\\.)?)|((linecoltolinecol|fromlinecol|tolinecol|linecol|fromline|toline|line|pos|column|col|extractByRegexLast|extractByRegex|extractNumberLast|extractNumber|extractDateLast|extractDate|extractLast|extract|token|before|after)\\(\\s*((((literal)[0-9]+)|((regex)[0-9]+))|[0-9]+|([0-9]+,\\s*[0-9]+)|([0-9]+(,\\s*[0-9]+){3}))\\s*\\))(\\.)?";
    private static final Pattern[] basicPatterns = new Pattern[]{tokenPattern, tolinecolPattern, fromlinecolPattern, linePattern, posPattern, colPattern, extractByRegexFirstLastPattern, extractFirstLastNPattern, extractFirstLastNumberFormatPattern, extractFirstLastNumberPattern, extractFirstLastDateFormatPattern, extractFirstLastDatePattern, beforeAfterPattern};
    private static final String[][] beforeArgKeyWords = new String[][]{{"token ", "t "}, {"line"}, {"line"}, {"line "}, {"position ", "pos "}, {"column ", "col "}, {"last ", "first ", "extract "}, {"last ", "first ", "extract "}, {"number"}, {"xxx"}, {"date"}, {"xxx"}, {"after ", "before "}};
    private String parsingRule = null;
    private String fromClause = null;
    private String extractClause = null;
    private Binding bindMap = null;
    private static final String[] methodNames = new String[]{"token", "tolinecol", "linecol", "line", "pos", "col", "extractByRegex", "extract", "extractNumber", "extractNumber", "extractDate", "extractDate", ""};

    public String polish(String rule, Binding bindMap) throws Exception {
        this.bindMap = bindMap;
        this.parsingRule = this.replaceLiteral(rule);
        if (rule.indexOf(" from ") > 0) {
            this.extractClause = this.parsingRule.substring(0, this.parsingRule.indexOf(" from "));
            this.fromClause = this.parsingRule.substring(this.parsingRule.indexOf(" from ") + 6);
            this.extractClause = this.polish(this.extractClause);
            ParsingScriptPolisher.validateSyntax(this.extractClause, bindMap);
            this.fromClause = this.polish(this.fromClause).trim();
            if (this.fromClause.indexOf("(") > 0 && this.fromClause.indexOf(")") > 0) {
                ParsingScriptPolisher.validateSyntax(this.fromClause, bindMap);
            }
            this.parsingRule = this.fromClause + "." + this.extractClause;
        } else {
            this.parsingRule = this.polish(this.parsingRule);
            ParsingScriptPolisher.validateSyntax(this.parsingRule, bindMap);
        }
        return this.parsingRule;
    }

    private String replaceLiteral(String parsingRule) {
        int literalCount = 0;
        try {
            literalCount = Integer.parseInt((String)this.bindMap.getProperty("literalStartIndex"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        StringBuffer value = new StringBuffer();
        StringBuffer token = null;
        boolean insideToken = false;
        char currentQuoteChar = '\u0000';
        for (int i = parsingRule.length() - 1; i >= 0; --i) {
            char c = parsingRule.charAt(i);
            if (!insideToken) {
                if (c == '\'' || c == '\"' || c == '/') {
                    insideToken = true;
                    token = new StringBuffer();
                    currentQuoteChar = c;
                    continue;
                }
                value.insert(0, c);
                continue;
            }
            if (c == currentQuoteChar && parsingRule.charAt(i - 1) != '\\') {
                String newVar = (c == '/' ? "regex" : "literal") + literalCount++;
                String literalStr = c == '/' ? "/" + token.toString() + "/" : token.toString();
                value.insert(0, newVar);
                this.bindMap.setVariable(newVar, (Object)literalStr);
                insideToken = false;
                continue;
            }
            if (c == '\\') {
                char afterChar = parsingRule.charAt(i + 1);
                if (afterChar == '\'' || afterChar == '\"' || afterChar == '/') continue;
                token.insert(0, c);
                continue;
            }
            token.insert(0, c);
        }
        this.bindMap.setProperty("literalStartIndex", (Object)("" + literalCount++));
        return value.toString();
    }

    private String polish(String input) {
        String returnscript;
        Matcher matcher = null;
        String s = input;
        matcher = lineposTolineposPattern.matcher(input);
        while (matcher.find()) {
            String matched = matcher.group();
            String matchedStr = matched.toLowerCase();
            int argIndex = matchedStr.indexOf("line") + 4;
            String methodName = "linecoltolinecol";
            s = ParsingScriptPolisher.replaceWithMethod(s, methodName, matched, argIndex);
        }
        for (int i = 0; i < basicPatterns.length; ++i) {
            matcher = basicPatterns[i].matcher(s);
            while (matcher.find()) {
                String matched = matcher.group();
                String matchedStr = matched.toLowerCase();
                String[] keywords = beforeArgKeyWords[i];
                int argIndex = -1;
                for (int k = 0; k < keywords.length; ++k) {
                    if (matchedStr.indexOf(keywords[k]) < 0) continue;
                    argIndex = matchedStr.indexOf(keywords[k]) + keywords[k].length();
                    break;
                }
                String methodName = methodNames[i] + (matchedStr.indexOf("last") >= 0 ? "Last" : (matchedStr.indexOf("before") >= 0 ? "Before" : (matchedStr.indexOf("after") >= 0 ? "After" : "")));
                s = ParsingScriptPolisher.replaceWithMethod(s, methodName, matched, argIndex);
            }
        }
        if (s.lastIndexOf(".") == s.length() - 1) {
            s = s.substring(0, s.length() - 1);
        }
        if ((returnscript = (s = s.trim())).indexOf("extract ") == 0) {
            returnscript = s.substring(s.indexOf(" ") + 1) + ".extract()";
        }
        if (returnscript.indexOf("extract") == 0 && s.indexOf(").") > 0) {
            returnscript = s.substring(s.indexOf(").") + 2) + "." + s.substring(0, s.indexOf(").") + 1);
        }
        if (returnscript.indexOf("from ") == 0) {
            returnscript = returnscript.substring(5).trim();
        }
        if (returnscript.indexOf("extract") == 0) {
            returnscript = s;
        }
        return returnscript.trim();
    }

    private static String replaceWithMethod(String s, String methodName, String matched, int argIndex) {
        char lastChar = matched.charAt(matched.length() - 1);
        if ("Before".equals(methodName)) {
            methodName = "before";
        } else if ("After".equals(methodName)) {
            methodName = "after";
        }
        String replaceS = methodName + "(" + (argIndex < 0 ? "" : ParsingScriptPolisher.cleanArg(matched.substring(argIndex))) + ")" + ParsingScriptPolisher.getChainingString(lastChar);
        s = StringUtil.replaceAll(s, matched, replaceS);
        return s;
    }

    private static String getChainingString(char lastChar) {
        return lastChar == ' ' || lastChar == ',' || lastChar == ';' ? "." : "";
    }

    private static String cleanArg(String arg) {
        char lastChar = (arg = arg.trim()).charAt(arg.length() - 1);
        if (lastChar == ',' || lastChar == ';') {
            arg = arg.substring(0, arg.length() - 1);
        }
        if ((arg = arg.replaceAll("\\s+((to\\s+line)|to|before|after|line|column|col)", ",")).indexOf(",") == 0) {
            arg = arg.substring(1);
        }
        return arg;
    }

    private static void validateSyntax(String input, Binding bindMap) throws Exception {
        String s = input.replaceAll(validPatternStr, "");
        if ((s = s.trim()).length() > 0) {
            for (int i = 0; i < 100; ++i) {
                try {
                    s = s.replaceAll("literal" + i, "'" + bindMap.getVariable("literal" + i) + "'");
                    s = s.replaceAll("regex" + i, "/" + bindMap.getVariable("regex" + i) + "/");
                    continue;
                }
                catch (MissingPropertyException missingPropertyException) {
                    // empty catch block
                }
            }
            throw new Exception("Unrecognized Syntax: " + s);
        }
    }
}

