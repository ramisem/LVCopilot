/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.instrument.textparser;

import com.labvantage.sapphire.pageelements.maint.RegexConverter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sapphire.util.M18NUtil;

public final class TextToken {
    public String LINE_SEPARATOR = System.getProperty("line.separator");
    public int LINE_SEPARATOR_LENGTH = this.LINE_SEPARATOR.length();
    private TextToken parentToken = null;
    private int posInParent = 0;
    private String text = "";
    private int absStartPos = 0;
    private int absEndPos = 0;
    private String numberPattern = "(((-|\\+)?[0-9]+(\\.[0-9]+)?)+(e|E)(-|\\+)[0-9]+)|((-|\\+)?[0-9]+(\\.[0-9]+)?)+";
    private static String datePattern = RegexConverter.getValidDateFormatExp(new M18NUtil(), false);

    public TextToken(String text, int posInParent, TextToken parentToken) {
        this.text = text;
        this.posInParent = posInParent;
        this.parentToken = parentToken;
        this.absEndPos = text.length();
        if (parentToken != null) {
            this.numberPattern = parentToken.numberPattern;
        }
    }

    public TextToken(String text, int posInParent, TextToken parentToken, int absStart, int absEnd) {
        this.text = text;
        this.posInParent = posInParent;
        this.parentToken = parentToken;
        this.absStartPos = absStart;
        if (parentToken != null) {
            this.numberPattern = parentToken.numberPattern;
            this.LINE_SEPARATOR = parentToken.LINE_SEPARATOR;
            this.LINE_SEPARATOR_LENGTH = parentToken.LINE_SEPARATOR_LENGTH;
        }
        this.absEndPos = absEnd > 0 ? absEnd : text.length();
    }

    public void setLINE_SEPARATOR(String line_separator) {
        this.LINE_SEPARATOR = line_separator;
        this.LINE_SEPARATOR_LENGTH = line_separator.length();
    }

    public void setDecimalSeparator(String decimalSeparator) {
        if (decimalSeparator != null && decimalSeparator.length() > 0 && !".".equals(decimalSeparator)) {
            this.numberPattern = "(((-|\\+)?[0-9]+(" + decimalSeparator + "[0-9]+)?)+(e|E)(-|\\+)[0-9]+)|((-|\\+)?[0-9]+(" + decimalSeparator + "[0-9]+)?)+";
        }
    }

    public void reset() {
        this.absStartPos = 0;
        this.absEndPos = this.text.length();
    }

    public int getAbsolutePos() {
        int absolutePos = this.absStartPos + this.posInParent;
        if (this.parentToken != null) {
            absolutePos += this.parentToken.getAbsolutePos();
        }
        return absolutePos;
    }

    public String getText() {
        String returntext = this.text.substring(this.absStartPos, this.absEndPos);
        if (returntext.length() > 0) {
            int count = 0;
            while (returntext.indexOf(" ") == 0) {
                returntext = returntext.substring(1);
                ++count;
            }
            while (returntext.lastIndexOf(" ") == returntext.length() - 1) {
                returntext = returntext.substring(0, returntext.length() - 1);
            }
            this.posInParent += count;
        }
        return returntext;
    }

    public TextToken line(int lineNo) {
        return this.line(lineNo, 1);
    }

    public TextToken line(int lineNo, int noOfLine) {
        int lineStartIndex = 0;
        int tempIndex = 0;
        int currentlineStartIndex = 1;
        String tempStr = this.text;
        String tokenText = "";
        if (lineNo > 1) {
            lineStartIndex = 0;
            while (tempStr.indexOf(this.LINE_SEPARATOR, tempIndex) >= 0) {
                lineStartIndex += tempStr.indexOf(this.LINE_SEPARATOR) + this.LINE_SEPARATOR_LENGTH;
                tempStr = tempStr.substring(tempStr.indexOf(this.LINE_SEPARATOR) + this.LINE_SEPARATOR_LENGTH);
                if (lineNo != ++currentlineStartIndex) continue;
            }
        }
        if (currentlineStartIndex < lineNo) {
            throw new RuntimeException("Line No " + lineNo + " does not exist");
        }
        StringBuffer lineBuffer = new StringBuffer();
        for (int i = 0; i < noOfLine; ++i) {
            if (tempStr.indexOf(this.LINE_SEPARATOR) >= 0) {
                tokenText = tempStr.substring(0, tempStr.indexOf(this.LINE_SEPARATOR));
                tempStr = tempStr.substring(tempStr.indexOf(this.LINE_SEPARATOR) + this.LINE_SEPARATOR.length());
            } else if (i == noOfLine - 1) {
                tokenText = tempStr;
            } else {
                throw new RuntimeException("Line No " + (lineNo + i) + " does not exist");
            }
            lineBuffer.append((i > 0 ? this.LINE_SEPARATOR : "") + tokenText);
        }
        return new TextToken(lineBuffer.toString(), lineStartIndex, this);
    }

    public TextToken fromline(int fromlineNo) {
        return this.fromlinecol(fromlineNo, 0);
    }

    public TextToken toline(int tolineNo) {
        return this.tolinecol(tolineNo, -1);
    }

    public TextToken linecol(int fromlineNo, int fromcol) {
        this.absStartPos = this.getLineStartIndex(fromlineNo) + fromcol;
        TextToken copy = new TextToken(this.text, 0, this.parentToken);
        copy.absStartPos = this.absStartPos;
        this.reset();
        return copy;
    }

    public TextToken fromlinecol(int fromlineNo, int fromcol) {
        return this.linecol(fromlineNo, fromcol);
    }

    public TextToken tolinecol(int tolineNo, int tocol) {
        if (tocol < 0) {
            tocol = this.line(tolineNo).getText().length();
        }
        this.absEndPos = this.getLineStartIndex(tolineNo) + tocol;
        return this.extract();
    }

    public TextToken linecoltolinecol(int fromlineNo, int tolineNo) {
        return this.linecoltolinecol(fromlineNo, 0, tolineNo, 0);
    }

    public TextToken linecoltolinecol(int fromlineNo, int fromcol, int tolineNo, int tocol) {
        int abfrompos = this.getLineStartIndex(fromlineNo) + fromcol;
        int abtopos = this.getLineStartIndex(tolineNo) + tocol;
        return this.pos(abfrompos, abtopos);
    }

    public TextToken extract() {
        return this.extract(this.absEndPos - this.absStartPos);
    }

    private TextToken extractChildToken(String text, int posInParent, int absStart, int absEnd) {
        this.reset();
        return new TextToken(text, posInParent, this, absStart, absEnd);
    }

    public TextToken extract(int n) {
        if (n <= this.absEndPos - this.absStartPos) {
            return this.extractChildToken(this.text.substring(this.absStartPos, this.absStartPos + n), this.absStartPos, 0, 0);
        }
        return this.emptyToken();
    }

    public TextToken extractLast(int n) {
        if (n < this.absEndPos - this.absStartPos) {
            return this.extractChildToken(this.text.substring(this.absEndPos - n, this.absEndPos), this.absEndPos - n, 0, 0);
        }
        return this.emptyToken();
    }

    public TextToken extractByRegex(String regex) {
        String token = this.getMatchedToken(regex);
        if (token.length() > 0) {
            return this.extractChildToken(token, this.text.indexOf(token, this.absStartPos), 0, 0);
        }
        return this.emptyToken();
    }

    public TextToken extractByRegexLast(String regex) {
        String token = this.getLastMatchedToken(regex);
        if (token.length() > 0) {
            int posInParent = this.text.lastIndexOf(token, this.absEndPos);
            return this.extractChildToken(token, posInParent, 0, 0);
        }
        return this.emptyToken();
    }

    public TextToken extractNumber() {
        return this.extractByRegex(this.numberPattern);
    }

    public TextToken extractNumberLast() {
        return this.extractByRegexLast(this.numberPattern);
    }

    public TextToken extractNumber(String format) {
        String regex = this.convertNumberFormatToRegex(format);
        return this.extractByRegex(regex);
    }

    public TextToken extractNumberLast(String format) {
        String regex = this.convertNumberFormatToRegex(format);
        return this.extractByRegexLast(regex);
    }

    public TextToken extractDate() {
        return this.extractByRegex(datePattern);
    }

    public TextToken extractDateLast() {
        return this.extractByRegexLast(datePattern);
    }

    public TextToken extractDate(String format) {
        String regex = RegexConverter.convert(format, false);
        return this.extractByRegex("(?i)" + regex);
    }

    public TextToken extractDateLast(String format) {
        String regex = RegexConverter.convert(format, false);
        return this.extractByRegexLast("(?i)" + regex);
    }

    public TextToken col(int colNo) {
        return this.pos(colNo);
    }

    public TextToken col(int colNo, int endNo) {
        return this.pos(colNo, endNo);
    }

    public TextToken pos(int start) {
        this.absStartPos += start;
        return this.extract();
    }

    public TextToken pos(int start, int end) {
        this.absEndPos = this.absStartPos + end;
        this.absStartPos += start;
        return this.extract();
    }

    public TextToken before(String token) {
        String regex = TextToken.getRegex(token);
        if (regex.length() > 0) {
            token = this.getMatchedToken(regex);
        }
        if (token.length() == 0) {
            return this.emptyToken();
        }
        int foundIndex = this.text.indexOf(token, this.absStartPos);
        if (foundIndex < 0 || foundIndex > this.absEndPos) {
            return this.emptyToken();
        }
        this.absEndPos = foundIndex;
        return this.extractChildToken(this.text, 0, this.absStartPos, this.absEndPos);
    }

    public TextToken after(String token) {
        String regex = TextToken.getRegex(token);
        if (regex.length() > 0) {
            token = this.getMatchedToken(regex);
        }
        if (token.length() == 0) {
            return this.emptyToken();
        }
        int foundIndex = this.text.indexOf(token, this.absStartPos);
        if (foundIndex < 0 || foundIndex > this.absEndPos) {
            return this.emptyToken();
        }
        this.absStartPos = foundIndex + token.length();
        return this.extractChildToken(this.text, 0, this.absStartPos, this.absEndPos);
    }

    private int getLineStartIndex(int lineNo) {
        int lineStartIndex = 0;
        int tempIndex = 0;
        int currentlineStartIndex = 1;
        String tempStr = this.text;
        if (lineNo > 1) {
            lineStartIndex = 0;
            while (tempStr.indexOf(this.LINE_SEPARATOR, tempIndex) >= 0) {
                lineStartIndex += tempStr.indexOf(this.LINE_SEPARATOR) + this.LINE_SEPARATOR_LENGTH;
                tempStr = tempStr.substring(tempStr.indexOf(this.LINE_SEPARATOR) + this.LINE_SEPARATOR_LENGTH);
                if (lineNo != ++currentlineStartIndex) continue;
            }
        }
        if (currentlineStartIndex < lineNo) {
            throw new RuntimeException("Line No " + lineNo + " does not exist");
        }
        return lineStartIndex;
    }

    private boolean isValidIndex(String s, int start, int end) {
        return start >= 0 && end >= 0 && s != null && s.length() >= end;
    }

    private String getMatchedToken(String regex) {
        return this.getMatchedToken(regex, false);
    }

    private String getMatchedToken(String regex, boolean matchLast) {
        if (TextToken.getRegex(regex).length() > 0) {
            regex = TextToken.getRegex(regex);
        }
        Pattern pattern = Pattern.compile(regex);
        String textAfterStart = this.text;
        if (this.absStartPos >= 0) {
            textAfterStart = this.text.substring(this.absStartPos, this.absEndPos);
        }
        Matcher matcher = pattern.matcher(textAfterStart);
        String matched = "";
        while (matcher.find()) {
            matched = matcher.group();
            if (matchLast) continue;
            return matched;
        }
        return matched;
    }

    private String getLastMatchedToken(String regex) {
        return this.getMatchedToken(regex, true);
    }

    private TextToken emptyToken() {
        return new TextToken("", -1, null);
    }

    private String convertNumberFormatToRegex(String format) {
        return format.replaceAll("0", "\\\\d").replaceAll("\\.", "\\\\.").replaceAll("\\+", "\\\\+").replaceAll("\\-", "\\\\-");
    }

    public static String getRegex(String s) {
        if (s.indexOf("/") == 0 && s.lastIndexOf("/") == s.length() - 1) {
            return s.substring(1, s.length() - 1);
        }
        return "";
    }

    public String toString() {
        return this.getText();
    }

    public TextToken plus(TextToken t) {
        return new TextToken(this.getText() + t.getText(), -1, null);
    }

    static {
        datePattern = "(?i)" + datePattern.substring(1, datePattern.length() - 1);
    }
}

